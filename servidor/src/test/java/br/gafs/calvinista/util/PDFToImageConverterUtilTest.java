package br.gafs.calvinista.util;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gabriel on 26/11/2017.
 */
public class PDFToImageConverterUtilTest {

    @Test
    @Ignore
    public void testConversao() throws IOException, URISyntaxException, InterruptedException {
        // load a pdf from a byte buffer

        final PDFToImageConverterUtil.PDFConverter converter1 = new PDFToImageConverterUtil.PDFConverter(new File("C:\\Users\\Gabriel\\Downloads\\27.pdf"), 0, 5);
        final PDFToImageConverterUtil.PDFConverter converter2 = new PDFToImageConverterUtil.PDFConverter(new File("C:\\Users\\Gabriel\\Downloads\\23.pdf"), 0, 5);
        final PDFToImageConverterUtil.PDFConverter converter3 = new PDFToImageConverterUtil.PDFConverter(new File("C:\\Users\\Gabriel\\Downloads\\24.pdf"), 0, 5);

        final PDFToImageConverterUtil.PageHandler pageHandler = new PDFToImageConverterUtil.PageHandler() {
            @Override
            public void handle(int page, byte[] data) throws IOException {

                File file = new File("pagina_" + page + "_" + System.currentTimeMillis() + ".png");

                FileOutputStream fos = new FileOutputStream(file);

                fos.write(data);

                fos.close();

            }
        };

        List<Thread> ts = new ArrayList<>();

        ts.add(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    converter1.forEachPage(pageHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));

        ts.add(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    converter2.forEachPage(pageHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));

        ts.add(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    converter3.forEachPage(pageHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));

        for (Thread t : ts) {
            t.start();
        }

        for (Thread t : ts) {
            t.join();
        }

    }


}