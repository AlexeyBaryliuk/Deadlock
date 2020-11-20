package com.edu.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeadlockHome {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeadlockHome.class);

    public static void main(String[] args) {
        List<GeneralResource> generalResources = getGeneralResources();

        for (int i = 0; i < 10; i++) {

            List<GeneralResource> randomResource = getRandomResource(generalResources);

            GeneralResource generalResource1 = randomResource.get(0);
            GeneralResource generalResource2 = randomResource.get(1);

            String nameOfThread = "User" + i;
            LOGGER.info("    {}   resource1 = {}   resource2 = {}", nameOfThread
                                                                       , generalResource2.getName()
                                                                       , generalResource1.getName());

            Thread thread = new Thread(new MyRunnableFirst(generalResource1, generalResource2), nameOfThread);

                thread.start();
        }
    }
    public static List<GeneralResource> getGeneralResources(){

        AtomicInteger id = new AtomicInteger(1);
        Supplier <GeneralResource> newGeneralResource = GeneralResource::new;

        Function <GeneralResource,GeneralResource> setNameEquipment = (n) -> {
           if (n.getId()%2 == 0 ){
               n.setName("Printer" + id.get());
           }
           else {
               n.setName("Scanner" + id.get());
           }
           return n;
        };

        List<GeneralResource> generalResources = Stream.generate(newGeneralResource)
                .map(n -> n.setId(id.incrementAndGet()))
                .map(setNameEquipment)
                .limit(10)
                .collect(Collectors.toList());

        Supplier <GeneralResource> randomName = () -> {
            int value = (int)(Math.random() * generalResources.size());
            return generalResources.get(value);
        };

        return generalResources;
    }

    public static List<GeneralResource> getRandomResource(List<GeneralResource> generalResources){

        List<GeneralResource> generalResourceList = generalResources.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream();
                }))
                .limit(2)
                .collect(Collectors.toList());
        return generalResourceList;
    }

}

class MyRunnableFirst implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyRunnableFirst.class);

    private final GeneralResource generalResourceFirst;
    private final GeneralResource generalResourceSecond;

    MyRunnableFirst(GeneralResource generalResourceFirst
            , GeneralResource generalResourceSecond) {

        this.generalResourceFirst = generalResourceFirst;
        this.generalResourceSecond = generalResourceSecond;
    }

    @Override
    public void run() {

        String name = Thread.currentThread().getName();
        boolean flag = true;

        while (flag) {

            LOGGER.info("            {} acquiring lock on {}", name, generalResourceFirst.getName());
            synchronized (generalResourceFirst) {

                LOGGER.info("            {} acquired lock on  {}", name, generalResourceFirst.getName());

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                generalResourceFirst.printing();

                if (generalResourceFirst.res > 100) {

                    flag = false;
                }

                LOGGER.info("            {} acquiring lock on {}", name, generalResourceSecond.getName());
                synchronized (generalResourceSecond) {

                    LOGGER.info("            {} acquired lock on  {}", name, generalResourceSecond.getName());
                    LOGGER.info("            {} released lock on {}", name, generalResourceSecond.getName());
                }

            }LOGGER.info("            {} released lock on {}", name, generalResourceFirst.getName());
        }
        LOGGER.debug("            {} finished execution.", name);
    }
}

class GeneralResource{

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralResource.class);

    private Integer id;
    public Integer res;
    public String name;


    public Integer getId() {
        return id;
    }

    public GeneralResource setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public GeneralResource setName(String name) {
        res = 0;
        this.name = name;
        return this;
    }

    public void printing(){

            res ++;
        LOGGER.info("I've printed the page number {}", res);

    }

}