package app.model;

import app.service.SignService;

public class Chunk {
    private final String sign;
    private final String publicKey;
    private final String data;

    public Chunk(String sign, String publicKey, String data) {
        this.sign = sign;
        this.publicKey = publicKey;
        this.data = data;
    }

    public boolean verify() throws Exception {
        return SignService.verify(data, publicKey, sign);
    }

    @Override
    public String toString() {
        return "Chunk{" +
                "sign='" + sign + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
