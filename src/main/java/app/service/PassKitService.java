package app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.brendamour.jpasskit.PKBarcode;
import de.brendamour.jpasskit.PKField;
import de.brendamour.jpasskit.PKPass;
import de.brendamour.jpasskit.enums.PKBarcodeFormat;
import de.brendamour.jpasskit.passes.PKGenericPass;
import de.brendamour.jpasskit.signing.*;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static de.brendamour.jpasskit.signing.PKSigningUtil.createSignedAndZippedPkPassArchive;
import static de.brendamour.jpasskit.signing.PKSigningUtil.loadSigningInformationFromPKCS12AndIntermediateCertificateStreams;

public class PassKitService {
    public static void createPass(String serialNumber, String message) throws Exception {
        String teamIdentifier = "9HK4QP6364";
        String passTypeIdentifier = "pass.com.nam.discountCoupon";
        String organizationName = "ThoughtWorks Ltd";
        String description = "NAM discount coupon";
        String logoText = "NAM";
        String appleWWDRCA = "certificates/WWDR.pem"; // this is apple's developer relation cert
        String privateKeyPath = "certificates/privateKey.p12"; // the private key you exported from keychain
        String privateKeyPassword = ""; // the password you used to export
        try {

            PKSigningInformation pkSigningInformation = new PKSigningInformationUtil().loadSigningInformationFromPKCS12AndIntermediateCertificate(privateKeyPath, privateKeyPassword, appleWWDRCA);

            PKPass pass = new PKPass();
            pass.setPassTypeIdentifier(passTypeIdentifier);
            pass.setAuthenticationToken("vxwxd7J8AlNNFPS8k0a0FfUFtq0ewzFdc");
            pass.setSerialNumber(serialNumber);
            pass.setTeamIdentifier(teamIdentifier); // replace this with your team ID
            pass.setOrganizationName(organizationName);
            pass.setDescription(description);
            pass.setLogoText(logoText);
            pass.setForegroundColor("rgb(255, 255, 255)");
            pass.setBackgroundColor("rgb(206, 140, 53)");

            PKBarcode barcode = new PKBarcode();
            barcode.setFormat(PKBarcodeFormat.PKBarcodeFormatPDF417);
            barcode.setMessageEncoding(Charset.forName("iso-8859-1"));
            barcode.setMessage(message);
            List<PKBarcode> barcodes = new ArrayList<PKBarcode>();
            barcodes.add(barcode);
            pass.setBarcodes(barcodes);

            PKGenericPass generic = new PKGenericPass();
            List<PKField> primaryFields = new ArrayList<PKField>();
            PKField member = new PKField();
            member.setKey("offer"); // some unique key for primary field
            member.setLabel("Any premium dog food");
            member.setValue("20% off"); // some value
            primaryFields.add(member);
            generic.setPrimaryFields(primaryFields);
            pass.setGeneric(generic);

            if (pass.isValid()) {
                String template_path = ClassLoader.getSystemResource("discountCoupon").getPath(); // replace with your folder with the icons
                IPKPassTemplate passTemplate = new PKPassTemplateFolder(template_path);
                ObjectMapper objectMapper = new ObjectMapper();
                PKFileBasedSigningUtil pkSigningUtil = new PKFileBasedSigningUtil(objectMapper);
                byte[] signedAndZippedPkPassArchive = pkSigningUtil.createSignedAndZippedPkPassArchive(pass, passTemplate, pkSigningInformation);
                String outputFile = "./discountCoupon.pkpass"; // change the name of the pass
                ByteArrayInputStream inputStream = new ByteArrayInputStream(signedAndZippedPkPassArchive);
                IOUtils.copy(inputStream, new FileOutputStream(outputFile));

            } else {
                System.out.println("the pass is NOT Valid man!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("failed!");
        }

    }
}
