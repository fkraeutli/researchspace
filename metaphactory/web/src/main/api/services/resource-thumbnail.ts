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

import { Rdf } from 'platform/api/rdf';
import { SemanticContext } from 'platform/api/components';

import { BaseResourceService } from './BaseResourceService';

const THUMBNAIL_SERVICE_URL = `/rest/data/rdf/utils/thumbnails/default`;
const service = new BaseResourceService(THUMBNAIL_SERVICE_URL);

export function getThumbnail(iri: Rdf.Iri, context?: SemanticContext) {
  return service.getResource(iri, context);
}

export function getThumbnails(iris: ReadonlyArray<Rdf.Iri>, context?: SemanticContext) {
  return service.getResources(iris, context);
}
