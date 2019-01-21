package de.jungblut.datastructure;

import com.google.common.base.Joiner;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class TextLineInputProviderTest {

    @Test
    public void testTextLineInputProvider() throws Exception {
        ArrayList<String> list = new ArrayList<>();
        list.add("lol");
        list.add("omg!");
        Path path = Files.createTempFile("testTextLineInputProvider", ".txt");
        Files.write(path, Joiner.on('\n').join(list).getBytes(),
                StandardOpenOption.WRITE);

        TextLineInputProvider prov = new TextLineInputProvider(path);
        Iterable<String> from = prov.iterate();
        int index = 0;
        for (String s : from) {
            assertEquals(list.get(index++), s);
        }
        Files.deleteIfExists(path);
    }

}
