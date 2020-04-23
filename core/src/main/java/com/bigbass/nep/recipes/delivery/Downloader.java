package com.bigbass.nep.recipes.delivery;

import com.badlogic.gdx.Gdx;
import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.twmacinta.util.MD5;

import javax.xml.bind.DatatypeConverter;


final public class Downloader {
    private static Integer downloadBatchSize = 2048;

    private Downloader(){};

    private static byte[] wget(String url) {
        return wgetMD5(url).getKey();
    }

    private static Pair<byte[], MD5> wgetMD5(String url) {
        InputStream httpIn = null;
        try {
            httpIn = new BufferedInputStream(new URL(url).openStream());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MD5 md5 = new MD5();
            byte[] data = new byte[Downloader.downloadBatchSize];
            boolean downloadComplete = false;
            int count = 0;
            while (!downloadComplete) {
                count = httpIn.read(data, 0, Downloader.downloadBatchSize);
                if (count <= 0) {
                    downloadComplete = true;
                } else {
                    md5.Update(data, 0, count);
                    outputStream.write(data);
                }
            }
            return new Pair<>(outputStream.toByteArray(), md5);
        } catch (IOException e) {
            System.out.println(String.format("IO error occurred while wgetting %s", url));
            return null;
        }
    }

    public static String getRoot() {
        return "cache/archives/";
    }

    public static void setDownloadBatchSize(Integer size) {
        Downloader.downloadBatchSize = size;
    }

    public static void download(PeerConfig config) throws StageException {
        final byte[][] md5Holder = new byte[1][1];
        Thread md5Thread = null;
        if (config.md5Url != null) {
            Runnable md5Task = () -> {
                md5Holder[0] = Downloader.wget(config.md5Url);
            };
            md5Thread = new Thread(md5Task);
            md5Thread.start();
        }


        final byte[] file;
        MD5 md5 = null;
        if (md5Thread != null) {
            Pair<byte[], MD5> pair = Downloader.wgetMD5(config.url);
            file = pair.getKey();
            md5 = pair.getValue();
        } else {
            file = Downloader.wget(config.url);
        }

        try {
            if (md5Thread != null) {
                md5Thread.join();
            }
        } catch (InterruptedException e) {
            System.out.println(String.format("md5 download thread for %s was interrupted, no guarantee for %s", config.md5Url, config.alias));
            return;
        }
        if (md5 != null) {
            if (md5Holder[0] == null) {
                System.out.println(String.format("Uncaught error occurred while downloading %s, no guarantee for %s", config.md5Url, config.alias));
                throw new StageException();
            } else {
                if (!MD5.hashesEqual(md5.Final(), DatatypeConverter.parseHexBinary(new String(md5Holder[0]).split(" ")[0]))) {
                    System.out.println(String.format("MD5 check failed for %s", config.alias));
                    throw new StageException();
                }
            }
        }
        Gdx.files.local(Downloader.getRoot() + config.alias).writeBytes(file, false);
    }
}
