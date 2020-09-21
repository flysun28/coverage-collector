/*******************************************************************************
 * Copyright (c) 2009, 2019 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marc R. Hoffmann - initial API and implementation
 *
 *******************************************************************************/
package com.oppo.jacocoreport.coverage.jacoco;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.data.ExecutionDataStore;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * This example reads Java class files, directories or JARs given as program
 * arguments and dumps information about the classes.
 */
public final class ClassInfo implements ICoverageVisitor {

    private final Analyzer analyzer;
    private Set<String> classIDSet;
    private Set<String> classNameSet;
    private String classpath;

    /**
     * Creates a new example instance printing to the given stream.
     *
     */
    public ClassInfo(final String classpath) {
        this.classIDSet = new HashSet();
        this.classNameSet = new HashSet<>();
        this.classpath = classpath;
        analyzer = new Analyzer(new ExecutionDataStore(), this);
    }


    /**
     * Run this example with the given parameters.
     *
     * @throws IOException in case of error reading a input file
     */
    public void execute() throws IOException {
        analyzer.analyzeAll(new File(this.classpath));
    }

    public void visitCoverage(final IClassCoverage coverage) {
//        out.printf("class name:   %s%n", coverage.getName());
//        out.printf("class id:     %016x%n", Long.valueOf(coverage.getId()));
        this.classIDSet.add(Long.toHexString(coverage.getId()));
        this.classNameSet.add(coverage.getName());
//        out.printf("instructions: %s%n", Integer.valueOf(coverage
//                .getInstructionCounter().getTotalCount()));
//        out.printf("branches:     %s%n",
//                Integer.valueOf(coverage.getBranchCounter().getTotalCount()));
//        out.printf("lines:        %s%n",
//                Integer.valueOf(coverage.getLineCounter().getTotalCount()));
//        out.printf("methods:      %s%n",
//                Integer.valueOf(coverage.getMethodCounter().getTotalCount()));
//        out.printf("complexity:   %s%n%n", Integer.valueOf(coverage
//                .getComplexityCounter().getTotalCount()));
    }

    public Set<String> getClassNameSet() {
        return classNameSet;
    }
    public Set<String> getClassIDSet() {
        return classIDSet;
    }
    /**
     * Entry point to run this examples as a Java application.
     *
     * @param args list of program arguments
     * @throws IOException in case of errors executing the example
     */
    public static void main(final String[] args) throws IOException {
        String execfile = "D:\\codeCoverage\\taskID\\10012\\classes";
        ClassInfo classInfo = new ClassInfo(execfile);
        classInfo.execute();
        Iterator iterator = classInfo.getClassNameSet().iterator();
        Iterator iterator1 = classInfo.getClassIDSet().iterator();
        while(iterator.hasNext()){
            System.out.println(iterator.next().toString());
            System.out.println(iterator1.next().toString());
        }
    }

}
