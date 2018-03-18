package app.model;

import app.service.SignService;

public class Chunk {
    public final String sign;
    public final String publicKey;
    public final String data;

    public Chunk(String sign, String publicKey, String data) {
        this.sign = sign;
        this.publicKey = publicKey;
        this.data = data;
    }

    public boolean verify() throws Exception {
        return SignService.Verify(data, publicKey, sign);
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "Sign='" + sign + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
