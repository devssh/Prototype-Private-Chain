package app.model;

import app.service.SignService;

public class Verifiable {
    public final String sign="304602210093a415fd1b3ee0ae465de66077ba52672389611ff95eb1ceeaa527a4a12be9d5022100b48f8c4e2d2e1c9498a308e3b99208a412e634117007079c4218f8b8f0000000";
    public final String publicKey="MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/tj7Digjq1sZ0We9vGOoq72MXk0rZ+ioA3bks6wYz2LRcxj2O6BIKdly+kS/uNJCIcL7LW4Gy2QGPosYj5JNsw==";
    public final String data="13663515MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/tj7Digjq1sZ0We9vGOoq72MXk0rZ+ioA3bks6wYz2LRcxj2O6BIKdly+kS/uNJCIcL7LW4Gy2QGPosYj5JNsw==3046022100cd450823c77a16e3aa95753ba6268245d01b87f8c75a8fe4530411bf037a71a2022100dda06c67f1ab99087c0dc90967788f1c12c55ac4d9e87b590ef315a9bc6ed77b";

    public boolean verify() throws Exception {
        return SignService.Verify(data, publicKey, sign);
    }
}
