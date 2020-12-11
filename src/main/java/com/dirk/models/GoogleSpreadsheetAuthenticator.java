/*
 * MIT License
 *
 * Copyright (c) 2020 Wesley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dirk.models;

import com.dirk.DirkApplication;
import com.dirk.helper.TournamentHelper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleSpreadsheetAuthenticator {
    private final Sheets service;
    private String spreadsheetId;

    public GoogleSpreadsheetAuthenticator() throws IOException, GeneralSecurityException {
        InputStream credentialsFile = DirkApplication.class.getClassLoader().getResourceAsStream("credentials.json");
        if (credentialsFile == null) {
            throw new FileNotFoundException("credentials.json not found");
        }

        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(credentialsFile));
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        List<String> scopes = Collections.singletonList(SheetsScopes.SPREADSHEETS);
        String tokenDirectoryPath = "tokens";

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new File(tokenDirectoryPath)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(7777).build();
        Credential credentials = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        this.service = new Sheets.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credentials)
                .setApplicationName("DirkBot reschedule")
                .build();
    }

    public GoogleSpreadsheetAuthenticator(String spreadsheetId) throws IOException, GeneralSecurityException {
        this();
        this.spreadsheetId = spreadsheetId;
    }

    /**
     * Create a message for when an exception occurs
     *
     * @param ex the exception
     * @return the message
     */
    public static String parseException(Exception ex) {
        String errorMessage;

        if (ex instanceof GoogleJsonResponseException) {
            int statusCode = ((GoogleJsonResponseException) ex).getStatusCode();

            switch (statusCode) {
                case 403:
                    errorMessage = "DirkBot does not have permission to read/write the spreadsheet. \n\n" +
                            "Share the spreadsheet with `" + TournamentHelper.DIRK_BOT_EMAIL + "` and give it editor permissions.";
                    break;
                case 404:
                    errorMessage = "Unable to find the spreadsheet. \n\n" +
                            "Make sure the spreadsheet url looks like this: " +
                            "`https://docs.google.com/spreadsheets/d/1yN-vwlhBEpdRJzSDRYM4IToLaXulLRrW_LYT-Hitd64/edit#gid=193799805`";
                    break;
                default:
                    errorMessage = "Unknown error, contact Wesley#2772 (GoogleJsonResponseException): " + ex.getMessage();
                    break;
            }
        } else {
            errorMessage = "Unknown error, contact Wesley#2772 (Exception): " + ex.getMessage();
        }

        ex.printStackTrace();

        return errorMessage;
    }

    /**
     * Change the spreadsheet
     *
     * @param spreadsheetId the spreadsheet to change to
     */
    public void setSpreadsheetId(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }

    /**
     * Get data from the given range
     *
     * @param range the range to get the data from
     * @return the data from the given range
     * @throws IOException the error when something fails
     */
    public List<List<Object>> getDataFromRange(String tab, String range) throws IOException {
        ValueRange response = this.service.spreadsheets().values()
                .get(this.spreadsheetId, tab + "!" + range)
                .execute();

        return response.getValues();
    }
}
