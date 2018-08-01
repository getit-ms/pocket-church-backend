package br.gafs.pocket.corporate.app.controller;

import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by Gabriel on 03/04/2018.
 */
@Getter
public class UploadArquivoDTO {
    @NotEmpty
    private String fileName;

    @NotEmpty
    private String data;

}
