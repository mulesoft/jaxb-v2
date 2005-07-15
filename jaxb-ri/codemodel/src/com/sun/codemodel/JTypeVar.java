/*
 * @(#)$Id: JTypeVar.java,v 1.4 2005-07-15 21:49:26 kohsuke Exp $
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.codemodel;

import java.util.Iterator;

/**
 * Type variable used to declare generics.
 * 
 * @see JGenerifiable
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public final class JTypeVar extends JClass implements JDeclaration {
    
    private final String name;
    
    private JClass bound;

    JTypeVar(JCodeModel owner, String _name) {
        super(owner);
        this.name = _name;
    }
    
    public String name() {
        return name;
    }

    public String fullName() {
        return name;
    }

    public JPackage _package() {
        return null;
    }
    
    /**
     * Adds a bound to this variable.
     * 
     * @return  this
     */
    public JTypeVar bound( JClass c ) {
        if(bound!=null)
            throw new IllegalArgumentException("type variable has an existing class bound "+bound);
        bound = c;
        return this;
    }

    /**
     * Returns the class bound of this variable.
     * 
     * <p>
     * If no bound is given, this method returns {@link Object}.
     */
    public JClass _extends() {
        if(bound!=null)
            return bound;
        else
            return owner().ref(Object.class);
    }

    /**
     * Returns the interface bounds of this variable.
     */
    public Iterator _implements() {
        return bound._implements();
    }

    public boolean isInterface() {
        return false;
    }

    public boolean isAbstract() {
        return false;
    }

    /**
     * Prints out the declaration of the variable.
     */
    public void declare(JFormatter f) {
        f.id(name);
        if(bound!=null)
            f.p("extends").g(bound);
    }


    protected JClass substituteParams(JTypeVar[] variables, JClass[] bindings) {
        for(int i=0;i<variables.length;i++)
            if(variables[i]==this)
                return bindings[i];
        return this;
    }

    public void generate(JFormatter f) {
        f.id(name);
    }
}
