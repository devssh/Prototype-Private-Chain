package app.service;

import com.ryantenney.passkit4j.Pass;
import com.ryantenney.passkit4j.PassSerializer;
import com.ryantenney.passkit4j.sign.PassSigner;
import com.ryantenney.passkit4j.sign.PassSignerImpl;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class PassKitService {
    public static void CreatePass() throws Exception {
        PassSigner signer = PassSignerImpl.builder()
                .keystore(new FileInputStream("/path/to/certificate.p12"), "password")
                .intermediateCertificate(new FileInputStream("/path/to/AppleWWDRCA.cer"))
                .build();

        Pass pass = new Pass()
                .passTypeIdentifier("pass.com.bouldercoffeeco.storeCard")
                .serialNumber("1a2b3c")
                .teamIdentifier("cafed00d");

        PassSerializer.writePkPassArchive(pass, signer, new FileOutputStream("mypass.pkpass"));
    }
}
