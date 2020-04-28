/**
 * ResearchSpace
 * Copyright (C) 2015-2020, © Trustees of the British Museum
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.researchspace.ldp;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.researchspace.data.rdf.PointedGraph;
import org.researchspace.data.rdf.container.AbstractLDPContainer;
import org.researchspace.data.rdf.container.LDPR;
import org.researchspace.repository.MpRepositoryProvider;
import org.researchspace.vocabulary.CrmInf;
import org.researchspace.vocabulary.LDP;

import com.google.common.base.Throwables;

/**
 * @author Artem Kozlov <ak@metaphacts.com>
 */
@LDPR(iri = PropositionSetsContainers.IRI_STRING)
public class PropositionSetsContainers extends AbstractLDPContainer {
    public static final String IRI_STRING = "http://www.researchspace.org/ontology/Propositions.Container";
    public static final IRI IRI = vf.createIRI(IRI_STRING);

    public PropositionSetsContainers(IRI iri, MpRepositoryProvider repositoryProvider) {
        super(iri, repositoryProvider);
    }

    @Override
    public void initialize() {
        if (!getReadConnection().hasOutgoingStatements(this.getResourceIRI())) {
            LinkedHashModel m = new LinkedHashModel();
            m.add(vf.createStatement(IRI, RDF.TYPE, LDP.Container));
            m.add(vf.createStatement(IRI, RDF.TYPE, LDP.Resource));
            m.add(vf.createStatement(IRI, RDFS.LABEL, vf.createLiteral("Propositions Set Container")));
            try {
                getRootContainer().add(new PointedGraph(IRI, m));
            } catch (RepositoryException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    @Override
    public Model getModel() throws RepositoryException {
        Model container = super.getModel();
        Model profiles = container.filter(null, LDP.contains, null);
        for (Statement profileStm : profiles) {
            container.addAll(getReadConnection().getStatements((Resource) profileStm.getObject(), RDFS.LABEL, null));
            container.addAll(getReadConnection().getStatements((Resource) profileStm.getObject(), RDFS.COMMENT, null));
        }
        return container;
    }

    /**
     * Overriding the default implementation, to add new resources as instances of
     * {@link I4_Proposition_Set}. This is needed to take care of special
     * context/named graph handling for proposition set resources.
     */
    @Override
    protected void add(PointedGraph pointedGraph, RepositoryConnection repConnection) throws RepositoryException {
        PointedGraph pg = addLdpContainerRelation(addProvenance(pointedGraph));
        repConnection.add(pg.getGraph(),
                new I4_Proposition_Set(pointedGraph.getPointer(), this.repositoryProvider).getContextIRI());
    }

    @Override
    protected PointedGraph addProvenance(PointedGraph pg) {
        // for now we don't add provenance to proposition
        // since propositions are sets of statements which should not
        // be mixed with with provenance statements
        return pg;
    }

    @Override
    public IRI getResourceType() {
        return CrmInf.I4_Proposition_Set;
    }
}