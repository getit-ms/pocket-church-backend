/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.gafs.calvinista.entity.domain;

/**
 *
 * @author Gabriel
 */
public enum TemplateIgreja {
    _1("theme1"),
    _2("theme2"),
    _3("theme3"),
    _4("theme4");
    
    private final String name;

    private TemplateIgreja(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
