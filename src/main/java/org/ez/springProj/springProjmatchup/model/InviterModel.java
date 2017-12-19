package org.ez.springProj.springProjmatchup.model;

import lombok.Data;

@Data
public class InviterModel {

    private String id;
    private String email;
    private String displayName;

    public InviterModel(String id, String email, String displayName) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
    }
}
