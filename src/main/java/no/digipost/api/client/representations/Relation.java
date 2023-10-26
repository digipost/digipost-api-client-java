/*
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.api.client.representations;

public enum Relation {
    SELF,
    SELF_UPDATE,
    SELF_DELETE,
    ADD_CONTENT,
    SEND,
    SEARCH,
    AUTOCOMPLETE,
    CREATE_MESSAGE,
    API_DOCUMENTATION,
    GET_ENCRYPTION_KEY,
    GET_PRINT_ENCRYPTION_CERTIFICATE,
    IDENTIFY_RECIPIENT,
    IDENTIFY_RECIPIENT_WITH_ENCRYPTION_KEY,
    DOCUMENT_EVENTS,
    UNSUPPORTED,
    GET_DOCUMENT_CONTENT,
    GET_SENDER_INFORMATION,
    DUPLICATE_DOCUMENT,
    GET_INBOX,
    ADD_DATA,
    ARCHIVE_DOCUMENTS,
    GET_ARCHIVES,
    NEXT_DOCUMENTS,
    GET_ARCHIVE_DOCUMENTS_BY_REFERENCEID,
    GET_ARCHIVE_DOCUMENT_BY_UUID,
    DELETE_ARCHIVE_DOCUMENT_BY_UUID,
    GET_ARCHIVE_DOCUMENT_CONTENT,
    GET_ARCHIVE_DOCUMENT_CONTENT_STREAM,
    ADD_UNIQUE_UUID,
    CREATE_BATCH,
	GET_BATCH,
	COMPLETE_BATCH,
    CREATE_OR_ACTIVATE_USER_ACCOUNT,
    ADD_TAG,
    REMOVE_TAG,
    GET_TAGS,
    GET_SHARED_DOCUMENTS_STATE,
    GET_SHARED_DOCUMENT_CONTENT,
    GET_SHARED_DOCUMENT_CONTENT_STREAM,

}
