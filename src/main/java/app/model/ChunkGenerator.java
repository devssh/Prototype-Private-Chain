package app.model;

import app.service.SignService;

public class ChunkGenerator {
    public static Chunk GenerateChunk(Keyz keyz, String data) throws Exception {
        return new Chunk(SignService.Sign(keyz.privateKey, data), keyz.publicKey, data);
    }
}
