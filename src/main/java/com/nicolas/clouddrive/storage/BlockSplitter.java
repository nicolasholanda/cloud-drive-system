package com.nicolas.clouddrive.storage;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class BlockSplitter {

    public static final int BLOCK_SIZE_BYTES = 4 * 1024 * 1024;

    public List<byte[]> split(InputStream inputStream) throws IOException {
        List<byte[]> blocks = new ArrayList<>();
        byte[] buffer = new byte[BLOCK_SIZE_BYTES];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            if (bytesRead == BLOCK_SIZE_BYTES) {
                blocks.add(buffer.clone());
            } else {
                ByteArrayOutputStream partial = new ByteArrayOutputStream(bytesRead);
                partial.write(buffer, 0, bytesRead);
                blocks.add(partial.toByteArray());
            }
        }

        return blocks;
    }
}
