/**
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
package no.digipost.api.client.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.DigipostClientException;
import no.digipost.api.client.EventLogger;
import no.digipost.api.client.representations.AuthenticationLevel;
import no.digipost.api.client.representations.DigipostAddress;
import no.digipost.api.client.representations.Message;

import org.apache.commons.io.FileUtils;

public class DigipostSwingClient {

	private static final String BREV = "BREV";
	private static final String CERT = "CERT";
	private JFrame frmDigipostApiClient;
	private JTextField certField;
	private JPasswordField passwordField;
	private JTextField senderField;
	private JTextField subjectField;
	private JTextField recipientField;
	private JTextField contentField;
	private JTextArea logTextArea;

	private DigipostClient client;
	private final EventLogger eventLogger = new EventLogger() {

		@Override
		public void log(final String logMesssage) {
			logTextArea.append(logMesssage + "\n");
		}
	};
	private JTextField endpointField;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					DigipostSwingClient window = new DigipostSwingClient();
					window.frmDigipostApiClient.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public DigipostSwingClient() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmDigipostApiClient = new JFrame();
		frmDigipostApiClient.setTitle("Digipost API Client");
		frmDigipostApiClient.setSize(768, 576);
		frmDigipostApiClient.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frmDigipostApiClient.setLocationRelativeTo(null); // Sentrerer vinduet
		final JPanel contentPane = new JPanel();
		contentPane.setLayout(new CardLayout(0, 0));

		final JPanel brevPanel = new JPanel();
		brevPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		contentPane.add(brevPanel, BREV);
		brevPanel.setLayout(new BorderLayout(0, 0));

		JPanel brevTopPanel = new JPanel();
		brevTopPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
		brevPanel.add(brevTopPanel, BorderLayout.NORTH);
		brevTopPanel.setLayout(new BorderLayout(0, 0));

		JPanel brevHelpPanel = new JPanel();
		brevHelpPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
		brevTopPanel.add(brevHelpPanel, BorderLayout.NORTH);
		brevHelpPanel.setLayout(new BorderLayout(0, 0));

		JLabel steg2Label = new JLabel("Steg 2: Lag og send brev.");
		steg2Label.setFont(new Font("Dialog", Font.BOLD, 16));
		brevHelpPanel.add(steg2Label, BorderLayout.NORTH);

		JLabel steg2SubLabel = new JLabel(
				"<html><br>Her spesifiserer du selve forsendelsen du ønsker å sende. <br><br>Fra dette eksempel-GUI-et kan du kun sende "
						+ "til en persons Digipost-adresse, men dersom du bruker API-et, kan du også sende til en persons fødselsnummer. "
						+ "Les mer i dokumentasjonen.</html>");
		brevHelpPanel.add(steg2SubLabel, BorderLayout.SOUTH);

		JPanel brevMainPanel = new JPanel();
		brevTopPanel.add(brevMainPanel, BorderLayout.SOUTH);
		GridBagLayout gbl_brevMainPanel = new GridBagLayout();
		gbl_brevMainPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_brevMainPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_brevMainPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_brevMainPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		brevMainPanel.setLayout(gbl_brevMainPanel);

		JLabel emneLabel = new JLabel("Brev-emne");
		GridBagConstraints gbc_emneLabel = new GridBagConstraints();
		gbc_emneLabel.anchor = GridBagConstraints.EAST;
		gbc_emneLabel.insets = new Insets(0, 0, 5, 5);
		gbc_emneLabel.gridx = 0;
		gbc_emneLabel.gridy = 0;
		brevMainPanel.add(emneLabel, gbc_emneLabel);

		subjectField = new JTextField();
		GridBagConstraints gbc_emneField = new GridBagConstraints();
		gbc_emneField.insets = new Insets(0, 0, 5, 5);
		gbc_emneField.fill = GridBagConstraints.HORIZONTAL;
		gbc_emneField.gridx = 1;
		gbc_emneField.gridy = 0;
		brevMainPanel.add(subjectField, gbc_emneField);
		subjectField.setColumns(10);

		JLabel mottakerLabel = new JLabel("Mottakers digipostadresse");
		GridBagConstraints gbc_mottakerLabel = new GridBagConstraints();
		gbc_mottakerLabel.anchor = GridBagConstraints.EAST;
		gbc_mottakerLabel.insets = new Insets(0, 0, 5, 5);
		gbc_mottakerLabel.gridx = 0;
		gbc_mottakerLabel.gridy = 1;
		brevMainPanel.add(mottakerLabel, gbc_mottakerLabel);

		recipientField = new JTextField();
		GridBagConstraints gbc_mottakerField = new GridBagConstraints();
		gbc_mottakerField.insets = new Insets(0, 0, 5, 5);
		gbc_mottakerField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mottakerField.gridx = 1;
		gbc_mottakerField.gridy = 1;
		brevMainPanel.add(recipientField, gbc_mottakerField);
		recipientField.setColumns(10);

		JLabel innholdLabel = new JLabel("Brevets innhold (pdf)");
		GridBagConstraints gbc_innholdLabel = new GridBagConstraints();
		gbc_innholdLabel.anchor = GridBagConstraints.EAST;
		gbc_innholdLabel.insets = new Insets(0, 0, 5, 5);
		gbc_innholdLabel.gridx = 0;
		gbc_innholdLabel.gridy = 2;
		brevMainPanel.add(innholdLabel, gbc_innholdLabel);

		contentField = new JTextField();
		GridBagConstraints gbc_innholdField = new GridBagConstraints();
		gbc_innholdField.insets = new Insets(0, 0, 5, 5);
		gbc_innholdField.fill = GridBagConstraints.HORIZONTAL;
		gbc_innholdField.gridx = 1;
		gbc_innholdField.gridy = 2;
		brevMainPanel.add(contentField, gbc_innholdField);
		contentField.setColumns(10);

		JButton velgInnholdButton = new JButton("Velg...");
		velgInnholdButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				JFileChooser filChooser = new JFileChooser();
				filChooser.showOpenDialog(brevPanel);
				File filFile = filChooser.getSelectedFile();
				if (filFile != null) {
					contentField.setText(filFile.toString());
				}
			}
		});
		GridBagConstraints gbc_chooseContentButton = new GridBagConstraints();
		gbc_chooseContentButton.anchor = GridBagConstraints.WEST;
		gbc_chooseContentButton.insets = new Insets(0, 0, 5, 0);
		gbc_chooseContentButton.gridx = 2;
		gbc_chooseContentButton.gridy = 2;
		brevMainPanel.add(velgInnholdButton, gbc_chooseContentButton);

		JButton sendButton = new JButton("Send brev");
		sendButton.setFont(new Font("Dialog", Font.BOLD, 14));
		sendButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					Message message = createMessage(subjectField.getText(), recipientField.getText());
					client.sendMessage(message, FileUtils.openInputStream(new File(contentField.getText())));
				} catch (IOException ex) {
					eventLogger.log(ex.getMessage() + "\n");
				} catch (DigipostClientException ex) {
					eventLogger.log("\nDigipostClient kastet exception. \nFeilkode: " + ex.getErrorType() + "\nFeilmelding: "
							+ ex.getErrorMessage());
				}
			}
		});

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_1.gridx = 2;
		gbc_verticalStrut_1.gridy = 3;
		brevMainPanel.add(verticalStrut_1, gbc_verticalStrut_1);

		JButton btnBack = new JButton("Tilbake");
		btnBack.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				CardLayout layout = (CardLayout) contentPane.getLayout();
				layout.show(contentPane, CERT);
			}
		});
		GridBagConstraints gbc_btnBack = new GridBagConstraints();
		gbc_btnBack.anchor = GridBagConstraints.EAST;
		gbc_btnBack.insets = new Insets(0, 0, 0, 5);
		gbc_btnBack.gridx = 1;
		gbc_btnBack.gridy = 4;
		brevMainPanel.add(btnBack, gbc_btnBack);
		GridBagConstraints gbc_sendButton = new GridBagConstraints();
		gbc_sendButton.gridx = 2;
		gbc_sendButton.gridy = 4;
		brevMainPanel.add(sendButton, gbc_sendButton);

		JPanel logPanel = new JPanel();
		brevPanel.add(logPanel, BorderLayout.CENTER);
		logPanel.setLayout(new BorderLayout(0, 0));

		JScrollPane logScroll = new JScrollPane();
		logPanel.add(logScroll);

		logTextArea = new JTextArea();
		logTextArea.setLineWrap(true);
		logTextArea.setWrapStyleWord(true);
		logTextArea.setEditable(false);
		logScroll.setViewportView(logTextArea);
		frmDigipostApiClient.setContentPane(contentPane);

		CardLayout layout = (CardLayout) contentPane.getLayout();
		layout.show(contentPane, CERT);

		final JPanel velgCertPanel = new JPanel();
		velgCertPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		contentPane.add(velgCertPanel, CERT);
		velgCertPanel.setLayout(new BorderLayout(0, 0));

		JPanel helpPanel = new JPanel();
		helpPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
		velgCertPanel.add(helpPanel, BorderLayout.NORTH);
		helpPanel.setLayout(new BorderLayout(0, 0));

		JLabel steg1Label = new JLabel("Steg 1: Velg Sertifikat");
		steg1Label.setFont(new Font("Dialog", Font.BOLD, 16));
		helpPanel.add(steg1Label);

		JLabel steg1SubLabel = new JLabel(
				"<html><br>Før du kan sende brev, må du laste inn sertifikatet som er knyttet til din virksomhets Digipost-konto."
						+ " Dette må være på .p12-formatet. <br><br>Hvis dette er et Buypass-sertifikat, og du enda ikke har lastet "
						+ "det opp til Digipost, kan du gjøre dette på <a href='https://www.digipost.no/virksomhet'>"
						+ "https://www.digipost.no/virksomhet</a>. Les mer om dette i dokumentasjonen.</html>");
		helpPanel.add(steg1SubLabel, BorderLayout.SOUTH);

		JPanel certPanel = new JPanel();
		velgCertPanel.add(certPanel, BorderLayout.CENTER);
		GridBagLayout gbl_certPanel = new GridBagLayout();
		gbl_certPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_certPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_certPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_certPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		certPanel.setLayout(gbl_certPanel);

		JLabel certLabel = new JLabel("Sertifikatfil (.p12)");
		GridBagConstraints gbc_certLabel = new GridBagConstraints();
		gbc_certLabel.anchor = GridBagConstraints.EAST;
		gbc_certLabel.insets = new Insets(0, 0, 5, 5);
		gbc_certLabel.gridx = 0;
		gbc_certLabel.gridy = 0;
		certPanel.add(certLabel, gbc_certLabel);

		certField = new JTextField();
		GridBagConstraints gbc_certField = new GridBagConstraints();
		gbc_certField.insets = new Insets(0, 0, 5, 5);
		gbc_certField.fill = GridBagConstraints.HORIZONTAL;
		gbc_certField.gridx = 1;
		gbc_certField.gridy = 0;
		certPanel.add(certField, gbc_certField);
		certField.setColumns(10);

		JButton certButton = new JButton("Velg...");
		certButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				JFileChooser filChooser = new JFileChooser();
				filChooser.showOpenDialog(brevPanel);
				File filFile = filChooser.getSelectedFile();
				if (filFile != null) {
					certField.setText(filFile.toString());
				}
			}
		});
		GridBagConstraints gbc_certButton = new GridBagConstraints();
		gbc_certButton.insets = new Insets(0, 0, 5, 0);
		gbc_certButton.gridx = 2;
		gbc_certButton.gridy = 0;
		certPanel.add(certButton, gbc_certButton);

		JLabel passordLabel = new JLabel("Sertifikatpassord");
		GridBagConstraints gbc_passordLabel = new GridBagConstraints();
		gbc_passordLabel.anchor = GridBagConstraints.EAST;
		gbc_passordLabel.insets = new Insets(0, 0, 5, 5);
		gbc_passordLabel.gridx = 0;
		gbc_passordLabel.gridy = 1;
		certPanel.add(passordLabel, gbc_passordLabel);

		passwordField = new JPasswordField();
		GridBagConstraints gbc_passordField = new GridBagConstraints();
		gbc_passordField.insets = new Insets(0, 0, 5, 5);
		gbc_passordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passordField.gridx = 1;
		gbc_passordField.gridy = 1;
		certPanel.add(passwordField, gbc_passordField);
		passwordField.setColumns(10);

		JLabel avsenderLabel = new JLabel("Avsenders ID");
		GridBagConstraints gbc_avsenderLabel = new GridBagConstraints();
		gbc_avsenderLabel.anchor = GridBagConstraints.EAST;
		gbc_avsenderLabel.insets = new Insets(0, 0, 5, 5);
		gbc_avsenderLabel.gridx = 0;
		gbc_avsenderLabel.gridy = 2;
		certPanel.add(avsenderLabel, gbc_avsenderLabel);

		senderField = new JTextField();
		GridBagConstraints gbc_avsenderField = new GridBagConstraints();
		gbc_avsenderField.insets = new Insets(0, 0, 5, 5);
		gbc_avsenderField.fill = GridBagConstraints.HORIZONTAL;
		gbc_avsenderField.gridx = 1;
		gbc_avsenderField.gridy = 2;
		certPanel.add(senderField, gbc_avsenderField);
		senderField.setColumns(10);

		JLabel endpointLabel = new JLabel("API-endpoint URL");
		GridBagConstraints gbc_endpointLabel = new GridBagConstraints();
		gbc_endpointLabel.anchor = GridBagConstraints.EAST;
		gbc_endpointLabel.insets = new Insets(0, 0, 5, 5);
		gbc_endpointLabel.gridx = 0;
		gbc_endpointLabel.gridy = 3;
		certPanel.add(endpointLabel, gbc_endpointLabel);

		endpointField = new JTextField("https://api.digipost.no");
		GridBagConstraints gbc_endpointField = new GridBagConstraints();
		gbc_endpointField.insets = new Insets(0, 0, 5, 5);
		gbc_endpointField.fill = GridBagConstraints.HORIZONTAL;
		gbc_endpointField.gridx = 1;
		gbc_endpointField.gridy = 3;
		certPanel.add(endpointField, gbc_endpointField);
		endpointField.setColumns(10);

		JButton nesteButton = new JButton("Neste");
		nesteButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				CardLayout layout = (CardLayout) contentPane.getLayout();
				layout.show(contentPane, BREV);

				turnOffEndpointSslValidationIfWeAreTargetingDigipostTestEnvironment(endpointField.getText());

				try {
					client = new DigipostClient(endpointField.getText(), Long.parseLong(senderField.getText()), FileUtils
							.openInputStream(new File(certField.getText())), new String(passwordField.getPassword()), eventLogger);
				} catch (NumberFormatException e1) {
					eventLogger.log("FEIL: Avsenders ID må være et tall > 0");
				} catch (IOException e1) {
					eventLogger.log("FEIL: Klarte ikke å lese sertifikatfil:\n" + e1);
				} catch (Exception e1) {
					eventLogger.log("FEIL: Kunne ikke initialisere Digipost-API-klienten. Dette kan f.eks skyldes at"
							+ " sertifikatfilen var ugyldig, eller at du skrev inn feil passord. Feilmelding var:\n" + e1.getMessage());
				}
			}
		});

		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 2;
		gbc_verticalStrut.gridy = 4;
		certPanel.add(verticalStrut, gbc_verticalStrut);
		GridBagConstraints gbc_nesteButton = new GridBagConstraints();
		gbc_nesteButton.gridx = 2;
		gbc_nesteButton.gridy = 5;
		certPanel.add(nesteButton, gbc_nesteButton);

		CardLayout l = (CardLayout) contentPane.getLayout();
		l.show(contentPane, CERT);
	}

	private Message createMessage(final String subject, final String address) {
		return new Message(String.valueOf(System.currentTimeMillis()), subject, new DigipostAddress(address), false,
				AuthenticationLevel.PASSWORD, false);
	}

	/**
	 * Dersom vi tester mot et av Digiposts testmiljøer, vil vi ikke bruke
	 * SSL-validering.
	 */
	private void turnOffEndpointSslValidationIfWeAreTargetingDigipostTestEnvironment(final String endpoint) {
		if (endpoint.contains("camelon")) {
			eventLogger.log("Detekterte at vi går mot Digipost Testmiljø. Skrur derfor av SSL-sjekk");
			try {
				TrustManager[] noopTrustManager = new TrustManager[] { new X509TrustManager() {
					@Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					@Override
					public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
					}

					@Override
					public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
					}
				} };
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, noopTrustManager, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				HostnameVerifier noopHostnameVerifier = new HostnameVerifier() {
					@Override
					public boolean verify(final String hostname, final SSLSession session) {
						return true;
					}
				};
				HttpsURLConnection.setDefaultHostnameVerifier(noopHostnameVerifier);
			} catch (Exception e) {
				eventLogger.log("Klarte ikke å skru av SSL-sjekk.");
				throw new RuntimeException(e);
			}
		}
	}
}
