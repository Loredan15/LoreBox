package com.geekbrains.streamApi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Test {
    public static void main(String[] args) throws IOException {

        Foo sum = (left, right) -> left.intValue() + right.intValue();

        System.out.println(sum.foo(1, 2));

        Foo multiply = (l, r) -> l.intValue() * r.intValue();

        System.out.println(multiply.foo(12, 2));

        Consumer<Integer> printer = System.out::println;

//        Arrays.asList(1, 2, 3)
//                .forEach(x -> System.out.println("i = " + x));

        Predicate<Integer> evenPredicate = x -> x % 2 == 0;

        Stream.of(1, 2, 3, 4, 5, 6)
                .filter(evenPredicate)
                .forEach(printer);

        Supplier<Integer> zero = () -> 0;

        System.out.println(zero.get());

        Function<Integer, String> stringRepeat = x -> {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < x; i++) {
                s.append("a");
            }
            return s.toString();
        };

        Stream.of(1, 2, 3)
                .map(stringRepeat)
                .forEach(System.out::println);
//
//        Files.readAllLines(Paths.get("data.txt")).stream()
//                .flatMap(line -> Stream.of(line.split(" +")))
//                .filter(word -> !word.isEmpty())
//                .forEach(System.out::println);

        Integer res = Stream.of(1, 2, 3, 4, 5)
                .reduce(0, Integer::sum);

        System.out.println(res);

        ArrayList<Integer> list1 = Stream.of(1, 2, 3, 4, 5)
                .reduce(new ArrayList<>(),
                        (list, val) -> {
                            list.add(val);
                            return list;
                        },
                        (l, r) -> {
                            l.addAll(r);
                            return l;
                        }
                );

        System.out.println(list1);
        Path clientDir = Paths.get("client-sep-2021", "root");

        List<String> files = Files.list(clientDir)
                .filter(p -> !Files.isDirectory(p))
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());

        System.out.println(files);

        Integer x = 7;
        int y = x;

    }
}
