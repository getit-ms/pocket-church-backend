package br.gafs.pocket.corporate.util;

import br.gafs.pocket.corporate.entity.Arquivo;
import br.gafs.file.EntityFileManager;
import org.junit.Test;

import java.io.File;

/**
 * Created by Gabriel on 11/05/2018.
 */
public class PathArquivoTest {

    @Test
    public void testPathArquivo() {

        Arquivo arq = new Arquivo();

        arq.setId(33252L);

        File file = EntityFileManager.get(arq, "dados");

        System.out.println(">> " + file.getAbsolutePath());

    }

}
