package org.pf4j.demo.framework;

import org1.pf4j.demo.api.framework.Indexer;
import org1.pf4j.demo.api.framework.IndexerProvider;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * 索引管理，使用枚举类实现单例模式
 * 所有的索引插件，应该注册到本管理器上，
 * 具体业务逻辑后续补充，此时仅供参考。
 * Component manager for indexer
 *
 * @author sdai
 *
 */
public enum Indexers {
    INSTANCE;
    private Map<String, IndexerProvider> indexerProviders;
    private String defaultIndexerProvider;

    private Indexers() {
        indexerProviders = new HashMap<>();
        defaultIndexerProvider = "viperfish";
    }

    public void registerIndexerProvider(IndexerProvider p) {
        indexerProviders.put(p.getName(), p);
        p.registerConfig();
    }

    public Map<String, IndexerProvider> getIndexerProviders() {
        return this.indexerProviders;
    }

    public String getDefaultIndexerProvider() {
        return defaultIndexerProvider;
    }

    public void setDefaultIndexerProvider(String defaultIndexerProvider) {
        this.defaultIndexerProvider = defaultIndexerProvider;
    }

    public <T> Indexer<T> newIndexer() {
        return newIndexer(defaultIndexerProvider);
    }

    public <T> Indexer<T>  newIndexer(String instance) {
        Indexer<T> ind = null;

        try {
            // 通过反射获取model的真实类型
            ParameterizedType pt = (ParameterizedType) indexerProviders.get(defaultIndexerProvider).getClass().getGenericSuperclass();
            Class<Indexer<T>> clazz = (Class<Indexer<T>>) pt.getActualTypeArguments()[0];
            // 通过反射创建model的实例
            ind = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (ind != null) {
            return ind;
        }
//        for (Entry<String, IndexerProvider> j : indexerProviders.entrySet()) {
//            Indexer<T> indexer = j.getValue().newInstance(instance);
//            if (indexer != null) {
//                return indexer;
//            }
//        }
        return null;
    }

    public <T> Indexer<T> newIndexer(String provider, String instance) {
//        return indexerProviders.get(provider).newInstance(instance);
        return null;
    }

    public <T> Indexer<T> getIndexer() {
        return getIndexer(defaultIndexerProvider);
    }

    public <T> Indexer<T>  getIndexer(String instance) {
//        Indexer<T> ind = indexerProviders.get(defaultIndexerProvider).getInstance(instance);
//        if (ind != null) {
//            return ind;
//        }
//        for (Entry<String, IndexerProvider> j : indexerProviders.entrySet()) {
//            Indexer<T> indexer = j.getValue().getInstance(instance);
//            if (indexer != null) {
//                return indexer;
//            }
//        }
        return null;
    }

    public <T> Indexer<T>  getIndexer(String provider, String instance) {
//        return indexerProviders.get(provider).getInstance(instance);
        return null;
    }

    public void dispose() {
        for (Entry<String, IndexerProvider> j : indexerProviders.entrySet()) {
            j.getValue().dispose();
            System.err.println("disposed " + j.getKey());
        }
        indexerProviders.clear();
        System.err.println("disposed indexer providers");
    }

    public void refreshAll() {
        for (Entry<String, IndexerProvider> j : indexerProviders.entrySet()) {
            j.getValue().refresh();
        }
    }

}
