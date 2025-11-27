package jaredbgreat.climaticbiome.generation.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class CacheReference<T> extends WeakReference<T> {
        public CacheReference(T referent, ReferenceQueue<? super T> queue) {
                super(referent, queue);
        }
}
