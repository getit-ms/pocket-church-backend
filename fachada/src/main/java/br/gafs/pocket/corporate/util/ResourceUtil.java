/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.pocket.corporate.util;

import br.gafs.bundle.ResourceBundleUtil;
import java.io.File;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 *
 * @author Gabriel
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceUtil {
    public static String mensagem(String empresa, String path){
        return new File(new File(new File(ResourceBundleUtil._default().getPropriedade("RESOURCES_ROOT"), "mensagem"), empresa), path).getAbsolutePath();
    }
    
    public static String report(String empresa, String path){
        return new File(new File(new File(ResourceBundleUtil._default().getPropriedade("RESOURCES_ROOT"), "report"), empresa), path).getAbsolutePath();
    }
}
