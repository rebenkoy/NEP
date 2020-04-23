package com.bigbass.nep.recipes.delivery;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

final public class Unpacker {
    private static class ZipUnpackKernel {
        private static Integer downloadBatchSize = 2048;

        public static void setDownloadBatchSize(Integer size) {
            ZipUnpackKernel.downloadBatchSize = size;
        }

        public static void unpack(PeerConfig config) throws StageException {
            FileHandle file = Gdx.files.local(Downloader.getRoot() + config.alias);
            try {
                ZipInputStream zip = new ZipInputStream(new FileInputStream(file.path()));
                ZipEntry zipEntry = zip.getNextEntry();
                File output = Gdx.files.local(Unpacker.getRoot() + config.alias).file();
                output.mkdirs();
                byte[] buffer = new byte[ZipUnpackKernel.downloadBatchSize];
                while (zipEntry != null) {
                    File newFile = newFile(output, zipEntry);
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zip.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    zipEntry = zip.getNextEntry();
                }
                zip.closeEntry();
                zip.close();
            } catch (IOException e) {
                System.out.println(String.format("can not open file %s", file.path()));
                throw new StageException();
            }
        }

        private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
            File destFile = new File(destinationDir, zipEntry.getName());

            String destDirPath = destinationDir.getCanonicalPath();
            String destFilePath = destFile.getCanonicalPath();

            if (!destFilePath.startsWith(destDirPath + File.separator)) {
                throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
            }

            return destFile;
        }
    }


    public static String getRoot() {
        return "cache/unpacked/";
    }

    private static Map<String, BackingUpCache.StageExceptionFunction> kernelMapping;
    static {
        Unpacker.kernelMapping = new HashMap<>();
        Unpacker.kernelMapping.put("zip", ZipUnpackKernel::unpack);
    }

    public static void unpack(PeerConfig config) throws StageException {
        if (Unpacker.kernelMapping.containsKey(config.unpackKernel)) {
            Unpacker.kernelMapping.get(config.unpackKernel).apply(config);
        } else {
            System.out.println(String.format("Unknown unpacking kernel: '%s'", config.unpackKernel));
            throw new StageException();
        }
    }
}
