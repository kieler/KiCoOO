package de.cau.cs.kieler.kicooo;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import de.cau.cs.kieler.kicooo.KOptional.Some;
import de.cau.cs.kieler.kicooo.model.State.Reference;
import mjson.Json;

public sealed interface KOptional<T> permits KOptional.None, KOptional.Some {
    public static <T> KOptional<T> of(T value) {
        return new Some<>(value);
    }

    public static <T> KOptional<T> ofNullable(T value) {
        return value != null ? new Some<>(value) : new None<>();
    }

    public static <T> KOptional<T> empty() {
        return new None<>();
    }

    public static <T> KOptional<T> over(Optional<T> optional) {
        return optional.map(t -> (KOptional<T>) new Some<>(t)).orElse(new None<T>());
    }

    public boolean isPresent();

    public boolean isEmpty();

    public KOptional<T> filter(java.util.function.Predicate<? super T> predicate);

    public <U> KOptional<U> map(java.util.function.Function<? super T, ? extends U> mapper);

    public <U> KOptional<U> flatMap(java.util.function.Function<? super T, KOptional<U>> mapper);

    public T orElse(T other);

    public T orElseGet(java.util.function.Supplier<? extends T> other);

    public <X extends Throwable> T orElseThrow(java.util.function.Supplier<? extends X> exceptionSupplier) throws X;

    public record None<T>() implements KOptional<T> {

        @Override
        public String toString() {
            return "None";
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public KOptional<T> filter(java.util.function.Predicate<? super T> predicate) {
            return this;
        }

        @Override
        public <U> KOptional<U> map(java.util.function.Function<? super T, ? extends U> mapper) {
            return new None<>();
        }

        @Override
        public <U> KOptional<U> flatMap(java.util.function.Function<? super T, KOptional<U>> mapper) {
            return new None<>();
        }

        @Override
        public T orElse(T other) {
            return other;
        }

        @Override
        public T orElseGet(java.util.function.Supplier<? extends T> other) {
            return other.get();
        }

        @Override
        public <X extends Throwable> T orElseThrow(java.util.function.Supplier<? extends X> exceptionSupplier)
                throws X {
            throw exceptionSupplier.get();
        }
    }

    public record Some<T>(T value) implements KOptional<T> {
        public Some {
            Objects.requireNonNull(value);
        }

        @Override
        public String toString() {
            return "Some(" + value + ")";
        }

        @Override
        public boolean isPresent() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public KOptional<T> filter(Predicate<? super T> predicate) {
            if (predicate.test(value)) {
                return this;
            } else {
                return new None<>();
            }
        }

        @Override
        public <U> KOptional<U> map(Function<? super T, ? extends U> mapper) {
            return new Some<>(mapper.apply(value));
        }

        @Override
        public <U> KOptional<U> flatMap(Function<? super T, KOptional<U>> mapper) {
            var result = mapper.apply(value);
            return result;
        }

        @Override
        public T orElse(T other) {
            return value;
        }

        @Override
        public T orElseGet(Supplier<? extends T> other) {
            return value;
        }

        @Override
        public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            return value;
        }
    }
}