package org.apiteria.project.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Aspect
@Component
public class CachedAspect {

    private ConcurrentMap<CacheKey, CacheValue> cache = new ConcurrentHashMap<>();

    @Around("@annotation(org.apiteria.project.aspects.Cached)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Cached cachedAnnotation = method.getAnnotation(Cached.class);


        CacheKey key = new CacheKey(method, joinPoint.getArgs());


        CacheValue cachedValue = cache.get(key);

        clearIfNeeded(cachedAnnotation.size());
        if (cachedValue != null && !cachedValue.isExpired(cachedAnnotation.refreshUnit(), cachedAnnotation.refreshInterval())) {
            //System.out.println("Zwracam wynik z cache dla klucza: " + key);
            return cachedValue.getObject();
        }


        Object result = joinPoint.proceed(joinPoint.getArgs());
        //System.out.println("Zapisuję wynik do cache dla klucza: " + key);


        cache.put(key, new CacheValue(result));



        return result;
    }

    private void clearIfNeeded(int size){
        System.out.println(size+" "+cache.size());
        if(cache.size() >= size){
            System.out.println("czyszcze cache");
            cache = cache.entrySet().stream().sorted((obj,obj2)->{
                return obj.getValue().timestamp.compareTo(obj2.getValue().timestamp);}
            ).toList().subList(size/2,size)
                    .stream()
                    .collect(Collectors.toConcurrentMap(key->key.getKey(),key->key.getValue()));

        }

        return;
    }
    private static final class CacheKey {
        private final Method method;
        private final Object[] args;
        private final int hashCode;

        public CacheKey(Method method, Object[] args) {
            this.method = method;
            this.args = args;

            this.hashCode = Objects.hash(method, Arrays.deepHashCode(args));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return method.equals(cacheKey.method) &&
                    Arrays.deepEquals(args, cacheKey.args);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return "CacheKey{" +
                    "method=" + method.getName() +
                    ", args=" + Arrays.toString(args) +
                    '}';
        }
    }


    private static class CacheValue {
        private final Object object;
        private final Instant timestamp;

        CacheValue(Object object) {
            this.object = object;
            this.timestamp = Instant.now();
        }


        boolean isExpired(ChronoUnit unit, int interval) {
            Instant expiryTime = Instant.now().minus(interval, unit);
            return timestamp.isBefore(expiryTime);
        }

        public Object getObject() {
            return object;
        }
    }
}