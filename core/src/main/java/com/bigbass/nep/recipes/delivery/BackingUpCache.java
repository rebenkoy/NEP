package com.bigbass.nep.recipes.delivery;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;

public class BackingUpCache {
    @FunctionalInterface
    public interface StageExceptionFunction {
        void apply(PeerConfig t) throws StageException;
    }

    private static class RootCallPair {
        String root;
        StageExceptionFunction call;

        public RootCallPair(String root, StageExceptionFunction call) {
            this.root = root;
            this.call = call;
        }
    }

    private static ArrayList<RootCallPair> stages;

    static {
        stages = new ArrayList<>();
        stages.add(new RootCallPair(null, Downloader::download));
        stages.add(new RootCallPair(Downloader.getRoot(), Unpacker::unpack));
        stages.add(new RootCallPair(Unpacker.getRoot(), Importer::imp));
        stages.add(new RootCallPair(Importer.getRoot(), null));
    }

    public static void chain(PeerConfig config) {
        StageException exception = null;
        int backOffIndex = BackingUpCache.stages.size() - 1;
        while (backOffIndex >= 0) {
            if (BackingUpCache.stages.get(backOffIndex).root == null || Gdx.files.local(BackingUpCache.stages.get(backOffIndex).root + config.alias).exists()) {
                try {
                    for (int i = backOffIndex; i < BackingUpCache.stages.size(); ++i) {
                        if (BackingUpCache.stages.get(i).call != null) {
                            BackingUpCache.stages.get(i).call.apply(config);
                        }
                    }
                    return;
                } catch (StageException e) {
                    exception = e;
                }
            }
            --backOffIndex;
        }
        System.out.println(String.format("Could not get %s", config.alias));
        if (exception != null) {
            exception.printStackTrace();
        }
        System.exit(1);
    }
}
