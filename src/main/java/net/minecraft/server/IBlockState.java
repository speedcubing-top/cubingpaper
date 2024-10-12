package net.minecraft.server;

import java.util.Collection;

public interface IBlockState<T extends Comparable<T>> {

    String a();

    Collection<T> c();

    Class<T> b();

    String a(T t0);
    //Taco - Use-arrays-for-block-states
    // TacoSpigot start
    @SuppressWarnings("Convert2Lambda") // We have to use anon for performance reasons :/
    public static final net.techcable.tacospigot.Indexer<IBlockState> INDEXER = new net.techcable.tacospigot.Indexer<IBlockState>() {
        @Override
        public int getId(IBlockState state) {
            return state.getId();
        }
    };

    public default void tryInitId() {}

    public int getId();

    public int getValueId(T value);

    public T getByValueId(int id);
    // TacoSpigot end
}
