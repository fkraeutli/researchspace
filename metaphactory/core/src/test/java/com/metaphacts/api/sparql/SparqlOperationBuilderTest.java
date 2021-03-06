/*
 * Copyright (C) 2015-2017, metaphacts GmbH
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, you can receive a copy
 * of the GNU Lesser General Public License from http://www.gnu.org/
 */

package com.metaphacts.api.sparql;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.metaphacts.junit.MetaphactsGuiceTestModule;
import com.metaphacts.junit.RepositoryRule;

/**
 * @author Johannes Trame <jt@metaphacts.com>
 */
@RunWith(JukitoRunner.class)
@UseModules(MetaphactsGuiceTestModule.class)
public class SparqlOperationBuilderTest {
    @Inject
    @Rule
    public RepositoryRule repositoryRule;
    
    @Rule
    public ExpectedException exception= ExpectedException.none();
    
    private static final ValueFactory vf = SimpleValueFactory.getInstance();
    
    private static final IRI metaphactsURI = vf.createIRI("http://www.metaphacts.com");
    
    private static final Model testModel = new LinkedHashModel(Lists.newArrayList(
           vf.createStatement(metaphactsURI, RDF.TYPE, FOAF.ORGANIZATION),
           vf.createStatement(metaphactsURI, FOAF.NAME, vf.createLiteral("metaphacts GmbH"))
            ));
    
    @Before
    public void addTriples() throws RepositoryException{
        try (RepositoryConnection con = repositoryRule.getRepository().getConnection()) {
            con.add(testModel);
        }
    }
    
    @Test
    public void testTuple() throws Exception{
        SparqlOperationBuilder<TupleQuery> builder = SparqlOperationBuilder.<TupleQuery> create("SELECT ?subject ?object WHERE {?subject a ?object} LIMIT 10", TupleQuery.class);
        try (RepositoryConnection con = repositoryRule.getRepository().getConnection()) {
            TupleQuery op = builder.build(con);
            assertTrue(op instanceof TupleQuery);
            TupleQueryResult tqr = null;
            try{
                tqr= op.evaluate();
                assertTrue(tqr.hasNext());
                BindingSet binding = tqr.next();
                assertEquals(metaphactsURI,binding.getValue("subject"));
                assertEquals(FOAF.ORGANIZATION,binding.getValue("object"));
                assertFalse(tqr.hasNext());
            }finally{
                tqr.close();
            }
        }
    }
    
    @Test
    public void testBoolean() throws Exception{
        SparqlOperationBuilder<BooleanQuery> builder = SparqlOperationBuilder.<BooleanQuery> create("ASK {?subject a <http://xmlns.com/foaf/0.1/Organization>}", BooleanQuery.class);
        try (RepositoryConnection con = repositoryRule.getRepository().getConnection()) {
            BooleanQuery op = builder.build(con);
            assertTrue(op instanceof BooleanQuery);
            assertTrue(op.evaluate());
        }
    }
    
    @Test
    public void testGraph() throws Exception{
        SparqlOperationBuilder<GraphQuery> builder = SparqlOperationBuilder.<GraphQuery> create("CONSTRUCT{?s ?p ?o} WHERE {?s ?p ?o}", GraphQuery.class);
        try (RepositoryConnection con = repositoryRule.getRepository().getConnection()) {
            GraphQuery op = builder.build(con);
            assertTrue(op instanceof GraphQuery);
            GraphQueryResult gqr = null;
            try{
                gqr= op.evaluate();
                assertTrue(Models.isomorphic(testModel, QueryResults.asModel(gqr)));
            }finally{
                gqr.close();
            }
        }
    }
    
    @Test
    public void testDescribe() throws Exception{
        SparqlOperationBuilder<GraphQuery> builder = SparqlOperationBuilder.<GraphQuery> create("DESCRIBE <"+metaphactsURI.stringValue()+">", GraphQuery.class);
        try (RepositoryConnection con = repositoryRule.getRepository().getConnection()) {
            GraphQuery op = builder.build(con);
            assertTrue(op instanceof GraphQuery);
            GraphQueryResult gqr = null;
            try{
                gqr= op.evaluate();
                assertTrue(Models.isomorphic(testModel, QueryResults.asModel(gqr)));
            }finally{
                gqr.close();
            }
        }
    }
    
    @Test
    public void testUpdate() throws Exception{
        SparqlOperationBuilder<Update> builder = SparqlOperationBuilder.<Update> create("DELETE{?s ?p ?o} WHERE {?s ?p ?o}", Update.class);
        try (RepositoryConnection con = repositoryRule.getRepository().getConnection()) {
            Update op = builder.build(con);
            assertTrue(op instanceof Update);
            assertEquals(2, con.size());
            op.execute();
            assertEquals(0, con.size());
        }
    }
    
    @Test
    public void testCastException() throws RepositoryException, MalformedQueryException{
        SparqlOperationBuilder<BooleanQuery> builder = SparqlOperationBuilder.<BooleanQuery>create("SELECT * WHERE {?a ?b ?c} LIMIT 10", BooleanQuery.class);

        try (RepositoryConnection con = repositoryRule.getRepository().getConnection()) {
            exception.expect(IllegalArgumentException.class);
            exception.expectMessage(containsString("Query is a SPARQL SELECT query. Expected a boolean query."));
            builder.build(con);
        }
        
    }
    
    @Test
    public void testResolveThis() throws RepositoryException, MalformedQueryException, QueryEvaluationException{
        SparqlOperationBuilder<BooleanQuery> builder = SparqlOperationBuilder.<BooleanQuery> create("ASK {?? a <http://xmlns.com/foaf/0.1/Organization>}", BooleanQuery.class);
        try (RepositoryConnection con = repositoryRule.getRepository().getConnection()) {
            BooleanQuery op = builder.resolveThis(metaphactsURI).build(con);
            assertTrue(op instanceof BooleanQuery);
            assertTrue(op.evaluate());
        }
    }
    
    @Test
    public void testResolveUser() throws RepositoryException, MalformedQueryException, QueryEvaluationException{
        SparqlOperationBuilder<BooleanQuery> builder = SparqlOperationBuilder.<BooleanQuery> create("ASK {?__useruri__ a <http://xmlns.com/foaf/0.1/Organization>}", BooleanQuery.class);
        try (RepositoryConnection con = repositoryRule.getRepository().getConnection()) {
            BooleanQuery op = builder.resolveUser((IRI) metaphactsURI).build(con);
            assertTrue(op instanceof BooleanQuery);
            assertTrue(op.evaluate());
        }
    }

}