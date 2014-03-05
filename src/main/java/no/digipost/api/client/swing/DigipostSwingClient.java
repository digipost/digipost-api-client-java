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

import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;

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
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import no.digipost.api.client.DigipostClient;
import no.digipost.api.client.DigipostClientException;
import no.digipost.api.client.EventLogger;
import no.digipost.api.client.OngoingDelivery;
import no.digipost.api.client.representations.DigipostAddress;
import no.digipost.api.client.representations.Document;
import no.digipost.api.client.representations.FileType;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.MessageRecipient;
import no.digipost.api.client.representations.NameAndAddress;
import no.digipost.api.client.representations.NorwegianAddress;
import no.digipost.api.client.representations.PersonalIdentificationNumber;
import no.digipost.api.client.representations.PrintDetails;
import no.digipost.api.client.representations.PrintDetails.PostType;
import no.digipost.api.client.representations.PrintRecipient;
import no.digipost.api.client.representations.SmsNotification;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;

public class DigipostSwingClient {
	private OngoingDelivery.SendableWithPrintFallback delivery = null;

	private static final String BREV = "BREV";
	private static final String CERT = "CERT";
	private JFrame frmDigipostApiClient;
	private JTextField certField;
	private JPasswordField passwordField;
	private JTextField senderField;
	private JTextField subjectField;
	private JTextField recipientDigipostAddressField;
	private JTextField recipientPersonalIdentificationNumberField;
	private JTextField recipientNameField;
	private JTextField recipientAddress1Field;
	private JTextField recipientAddress2Field;
	private JTextField recipientPostalcodeField;
	private JTextField recipientCityField;
	private JTextField recipientBirthDateField;
	private JTextField recipientPhoneNumberField;
	private JTextField recipientEmailAddressField;
	private JCheckBox fallbackToPrintCheckBox;
	private JCheckBox directToPrintCheckBox;
	private JTextField attachmentSubjectField;
	private JTextField attachmentContentField;
	private JButton addAttachmentContentButton;
	private JButton btnAddAttachment;
	private JButton sendButton;
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
		frmDigipostApiClient.setSize(768, 768);
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
				"<html><br>Her spesifiserer du selve forsendelsen du ønsker å sende. <br><br>Fra dette eksempel-GUI-et kan du sende "
						+ "til en persons Digipost-adresse, fødselsnummer eller navn og adresse. Ved sending til en persons navn og adresse kan du legge til tilleggsidentifikatorer"
						+ " (fødselsdato og epost-adresse eller telefonnummer) og velge å sende med fallback til print eller direkte til print.</html>");
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
		brevMainPanel.add(emneLabel, createGridBagConstraintsForLabel(0, 0));

		subjectField = new JTextField();
		brevMainPanel.add(subjectField, createGridBagConstraintsForField(1, 0));
		subjectField.setColumns(10);

		JLabel mottakerLabel = new JLabel("Mottakers digipostadresse");
		brevMainPanel.add(mottakerLabel, createGridBagConstraintsForLabel(0, 1));

		recipientDigipostAddressField = new JTextField();
		brevMainPanel.add(recipientDigipostAddressField, createGridBagConstraintsForField(1, 1));
		recipientDigipostAddressField.setColumns(10);

		final JRadioButton identifyOnDigipostAddress = new JRadioButton();
		identifyOnDigipostAddress.setSelected(true);
		brevMainPanel.add(identifyOnDigipostAddress, createGridBagConstraintsForRadioButton(5, 1));
		identifyOnDigipostAddress.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				enableDigipostAddressFields();
			}
		});

		JLabel mottakerFoedselsnummerLabel = new JLabel("Mottakers fødselsnummer");
		brevMainPanel.add(mottakerFoedselsnummerLabel, createGridBagConstraintsForLabel(0, 2));

		recipientPersonalIdentificationNumberField = new JTextField();
		recipientPersonalIdentificationNumberField.setEnabled(false);
		brevMainPanel.add(recipientPersonalIdentificationNumberField, createGridBagConstraintsForField(1, 2));
		recipientPersonalIdentificationNumberField.setColumns(10);

		final JRadioButton identifyOnPersonalIdentificationNumber = new JRadioButton();
		brevMainPanel.add(identifyOnPersonalIdentificationNumber, createGridBagConstraintsForRadioButton(5, 2));
		identifyOnPersonalIdentificationNumber.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				enablePersonalIdentificationNumberFields();
			}
		});

		JLabel mottakerNavnLabel = new JLabel("Mottakers navn");
		brevMainPanel.add(mottakerNavnLabel, createGridBagConstraintsForLabel(0, 3));

		recipientNameField = new JTextField();
		recipientNameField.setEnabled(false);
		brevMainPanel.add(recipientNameField, createGridBagConstraintsForField(1, 3));
		recipientNameField.setColumns(10);

		JRadioButton identifyOnNameAndAddress = new JRadioButton();
		brevMainPanel.add(identifyOnNameAndAddress, createGridBagConstraintsForField(5, 3));
		identifyOnNameAndAddress.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				enableNameAndAddressFields();
			}
		});

		ButtonGroup identifierGroup = new ButtonGroup();
		identifierGroup.add(identifyOnDigipostAddress);
		identifierGroup.add(identifyOnNameAndAddress);
		identifierGroup.add(identifyOnPersonalIdentificationNumber);

		JLabel mottakerAdresse1Label = new JLabel("Adresselinje 1");
		brevMainPanel.add(mottakerAdresse1Label, createGridBagConstraintsForLabel(0, 4));

		recipientAddress1Field = new JTextField();
		recipientAddress1Field.setEnabled(false);
		brevMainPanel.add(recipientAddress1Field, createGridBagConstraintsForField(1, 4));
		recipientAddress1Field.setColumns(10);

		JLabel mottakerAdresse2Label = new JLabel("Adresselinje 2");
		brevMainPanel.add(mottakerAdresse2Label, createGridBagConstraintsForLabel(0, 5));

		recipientAddress2Field = new JTextField();
		recipientAddress2Field.setEnabled(false);
		brevMainPanel.add(recipientAddress2Field, createGridBagConstraintsForField(1, 5));
		recipientAddress2Field.setColumns(10);

		JLabel mottakerPostnummerLabel = new JLabel("Postnummer");
		brevMainPanel.add(mottakerPostnummerLabel, createGridBagConstraintsForLabel(0, 6));

		recipientPostalcodeField = new JTextField();
		recipientPostalcodeField.setEnabled(false);
		brevMainPanel.add(recipientPostalcodeField, createGridBagConstraintsForField(1, 6, 1));
		recipientPostalcodeField.setColumns(10);

		JLabel mottakerPoststedLabel = new JLabel("Poststed");
		brevMainPanel.add(mottakerPoststedLabel, createGridBagConstraintsForLabel(2, 6));

		recipientCityField = new JTextField();
		recipientCityField.setEnabled(false);
		brevMainPanel.add(recipientCityField, createGridBagConstraintsForField(3, 6, 1));
		recipientCityField.setColumns(10);

		JLabel mottakerFoedselsdatoLabel = new JLabel("Fødselsdato (DD.MM.YYYY)");
		brevMainPanel.add(mottakerFoedselsdatoLabel, createGridBagConstraintsForLabel(0, 7));

		recipientBirthDateField = new JTextField();
		recipientBirthDateField.setEnabled(false);
		brevMainPanel.add(recipientBirthDateField, createGridBagConstraintsForField(1, 7, 1));
		recipientBirthDateField.setColumns(10);

		JLabel mottakerTelefonnummerLabel = new JLabel("Telefonnummer");
		brevMainPanel.add(mottakerTelefonnummerLabel, createGridBagConstraintsForLabel(2, 7));

		recipientPhoneNumberField = new JTextField();
		recipientPhoneNumberField.setEnabled(false);
		brevMainPanel.add(recipientPhoneNumberField, createGridBagConstraintsForField(3, 7, 1));
		recipientPhoneNumberField.setColumns(10);

		JLabel mottakerEpostadresseLabel = new JLabel("Epost-adresse");
		brevMainPanel.add(mottakerEpostadresseLabel, createGridBagConstraintsForLabel(0, 8));

		recipientEmailAddressField = new JTextField();
		recipientEmailAddressField.setEnabled(false);
		brevMainPanel.add(recipientEmailAddressField, createGridBagConstraintsForField(1, 8));
		recipientEmailAddressField.setColumns(10);

		JLabel fallbackToPrintLabel = new JLabel("Fallback til print");
		brevMainPanel.add(fallbackToPrintLabel, createGridBagConstraintsForLabel(0, 9));

		fallbackToPrintCheckBox = new JCheckBox();
		fallbackToPrintCheckBox.setEnabled(false);
		brevMainPanel.add(fallbackToPrintCheckBox, createGridBagConstraintsForField(1, 9, 1));

		JLabel directToPrintLabel = new JLabel("Direkte til print");
		brevMainPanel.add(directToPrintLabel, createGridBagConstraintsForLabel(2, 9));

		directToPrintCheckBox = new JCheckBox();
		directToPrintCheckBox.setEnabled(false);
		brevMainPanel.add(directToPrintCheckBox, createGridBagConstraintsForField(3, 9, 1));

		JLabel innholdLabel = new JLabel("Brevets innhold");
		brevMainPanel.add(innholdLabel, createGridBagConstraintsForLabel(0, 10));

		contentField = new JTextField();
		brevMainPanel.add(contentField, createGridBagConstraintsForField(1, 10));
		contentField.setColumns(10);

		final JFileChooser filChooser = new JFileChooser();
		filChooser.setApproveButtonMnemonic(KeyEvent.VK_ENTER);
		JButton velgInnholdButton = new JButton("Velg...");
		velgInnholdButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {

				filChooser.showOpenDialog(brevPanel);
				File filFile = filChooser.getSelectedFile();
				if (filFile != null) {
					contentField.setText(filFile.toString());
				}
			}
		});
		brevMainPanel.add(velgInnholdButton, createGridBagConstraints(GridBagConstraints.WEST, 5, 10));

		JLabel vedleggTittelLabel = new JLabel("Vedlegg-emne");
		brevMainPanel.add(vedleggTittelLabel, createGridBagConstraintsForLabel(0, 11));

		attachmentSubjectField = new JTextField();
		attachmentSubjectField.setEnabled(false);
		brevMainPanel.add(attachmentSubjectField, createGridBagConstraintsForField(1, 11, 1));
		attachmentSubjectField.setColumns(10);

		JLabel vedleggInnholdLabel = new JLabel("Vedlegg-innhold");
		brevMainPanel.add(vedleggInnholdLabel, createGridBagConstraintsForLabel(2, 11));

		attachmentContentField = new JTextField();
		attachmentContentField.setEnabled(false);
		brevMainPanel.add(attachmentContentField, createGridBagConstraintsForField(3, 11, 1));
		attachmentContentField.setColumns(10);

		addAttachmentContentButton = new JButton("Velg...");
		addAttachmentContentButton.setEnabled(false);
		addAttachmentContentButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				JOptionPane.showMessageDialog(brevPanel, "Opplasting av vedlegg støttes ikke p.t. av Swing-klienten.");
//				filChooser.showOpenDialog(brevPanel);
//				File filFile = filChooser.getSelectedFile();
//				if (filFile != null) {
//					attachmentContentField.setText(filFile.toString());
//				}
			}
		});
		brevMainPanel.add(addAttachmentContentButton, createGridBagConstraints(GridBagConstraints.WEST, 5, 11));

		JButton btnBack = new JButton("Tilbake");
		btnBack.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				CardLayout layout = (CardLayout) contentPane.getLayout();
				layout.show(contentPane, CERT);
			}
		});
		GridBagConstraints gbc_btnBack = new GridBagConstraints();
		gbc_btnBack.gridx = 0;
		gbc_btnBack.gridy = 12;
		brevMainPanel.add(btnBack, gbc_btnBack);

		btnAddAttachment = new JButton("Legg til vedlegg");
		btnAddAttachment.setEnabled(false);
		btnAddAttachment.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (delivery == null) {
					eventLogger.log("Du må opprette og laste opp innhold til en forsendelse før du kan legge til vedlegg.");
					return;
				}
				try {
					String subject = attachmentSubjectField.getText();
					FileType fileType = FileType.fromFilename(attachmentContentField.getText());
					Document attachment = new Document(UUID.randomUUID().toString(), subject, fileType, null, new SmsNotification(), PASSWORD, NORMAL);
					delivery.addContent(attachment, FileUtils.openInputStream(new File(attachmentContentField.getText())));
				} catch (IOException ex) {
					eventLogger.log(ex.getMessage() + "\n");
				} catch (DigipostClientException ex) {
					eventLogger.log("\nDigipostClient kastet exception. \nFeilkode: " + ex.getErrorType() + "\nFeilmelding: "
							+ ex.getErrorMessage());
				}
			}
		});

		GridBagConstraints gbc_btnAddAttachment = new GridBagConstraints();
		gbc_btnAddAttachment.gridx = 1;
		gbc_btnAddAttachment.gridy = 12;
		brevMainPanel.add(btnAddAttachment, gbc_btnAddAttachment);

		JButton btnAddContent = new JButton("Opprett og last opp brev");
		btnAddContent.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					Message message = null;
					String subject = subjectField.getText();
					FileType fileType = FileType.fromFilename(contentField.getText());
					Document primaryDocument = new Document(UUID.randomUUID().toString(), subject, fileType, null, new SmsNotification(), PASSWORD, NORMAL);
					if (identifyOnDigipostAddress.isSelected()) {
						String digipostAddress = recipientDigipostAddressField.getText();
						message = new Message(UUID.randomUUID().toString(), new DigipostAddress(digipostAddress), primaryDocument, new ArrayList<Document>());
					} else if (identifyOnPersonalIdentificationNumber.isSelected()) {
						String personalIdentificationNumber = recipientPersonalIdentificationNumberField.getText();
						message = new Message(UUID.randomUUID().toString(), new PersonalIdentificationNumber(personalIdentificationNumber), primaryDocument, new ArrayList<Document>());
					} else {
						String name = recipientNameField.getText();
						String addressline1 = recipientAddress1Field.getText();
						String addressline2 = recipientAddress2Field.getText().equals("") ? null : recipientAddress2Field.getText();
						String zipCode = recipientPostalcodeField.getText();
						String city = recipientCityField.getText();
						String birthDateAsString = recipientBirthDateField.getText();
						LocalDate birthDate = null;
						if (!birthDateAsString.equals("")) {
							birthDate = new LocalDate(Integer.parseInt(birthDateAsString.substring(6)), Integer.parseInt(birthDateAsString
									.substring(3, 5)), Integer.parseInt(birthDateAsString.substring(0, 2)));
						}
						String phoneNumber = recipientPhoneNumberField.getText().equals("") ? null : recipientPhoneNumberField.getText();
						String emailAddress = recipientEmailAddressField.getText().equals("") ? null : recipientEmailAddressField.getText();

						NameAndAddress nameAndAddress = new NameAndAddress(name, addressline1, addressline2, zipCode, city, birthDate,
								phoneNumber, emailAddress);

						NorwegianAddress norwegianAddress = new NorwegianAddress(addressline1, addressline2, zipCode, city);
						PrintRecipient printRecipient, returnAddress;
						returnAddress = printRecipient = new PrintRecipient(name, norwegianAddress);
						PrintDetails printDetails = new PrintDetails(printRecipient, returnAddress, PostType.B);

						MessageRecipient recipient;
						if (fallbackToPrintCheckBox.isSelected()) {
							recipient = new MessageRecipient(nameAndAddress, printDetails);
						} else if (directToPrintCheckBox.isSelected()) {
							recipient = new MessageRecipient(printDetails);
						} else {
							recipient = new MessageRecipient(nameAndAddress);
						}

						new Message(UUID.randomUUID().toString(), recipient, primaryDocument, new ArrayList<Document>());
					}
					delivery = client.createMessage(message).addContent(primaryDocument, FileUtils.openInputStream(new File(contentField.getText())));
					enableAttachmentFields(true);
					sendButton.setEnabled(true);
				} catch (IOException ex) {
					eventLogger.log(ex.getMessage() + "\n");
				} catch (DigipostClientException ex) {
					eventLogger.log("\nDigipostClient kastet exception. \nFeilkode: " + ex.getErrorType() + "\nFeilmelding: "
							+ ex.getErrorMessage());
				}
			}
		});

		GridBagConstraints gbc_btnAddContent = new GridBagConstraints();
		gbc_btnAddContent.gridx = 2;
		gbc_btnAddContent.gridy = 12;
		gbc_btnAddContent.gridwidth = 2;
		brevMainPanel.add(btnAddContent, gbc_btnAddContent);

		sendButton = new JButton("Send brev");
		sendButton.setEnabled(false);
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (delivery == null) {
					eventLogger.log("Du må opprette og laste opp innhold til en forsendelse før den kan sendes.");
					return;
				}
				delivery.send();
				enableAttachmentFields(false);
				sendButton.setEnabled(false);
				delivery = null;
			}
		});
		GridBagConstraints gbc_sendButton = new GridBagConstraints();
		gbc_sendButton.gridx = 5;
		gbc_sendButton.gridy = 12;
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
		certPanel.add(certLabel, createGridBagConstraintsForLabel(0, 0));

		certField = new JTextField();
		certPanel.add(certField, createGridBagConstraintsForField(1, 0, 1));
		certField.setColumns(10);

		JButton certButton = new JButton("Velg...");
		certButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
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
		certPanel.add(passordLabel, createGridBagConstraintsForLabel(0, 1));

		passwordField = new JPasswordField();
		certPanel.add(passwordField, createGridBagConstraintsForField(1, 1, 1));
		passwordField.setColumns(10);

		JLabel avsenderLabel = new JLabel("Avsenders ID");
		certPanel.add(avsenderLabel, createGridBagConstraintsForLabel(0, 2));

		senderField = new JTextField();
		certPanel.add(senderField, createGridBagConstraintsForField(1, 2, 1));
		senderField.setColumns(10);

		JLabel endpointLabel = new JLabel("API-endpoint URL");
		certPanel.add(endpointLabel, createGridBagConstraintsForLabel(0, 3));

		endpointField = new JTextField("https://api.digipost.no");
		certPanel.add(endpointField, createGridBagConstraintsForField(1, 3, 1));
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

	private void enableAttachmentFields(final boolean enabled) {
		clearAttachmentFields();
		attachmentContentField.setEnabled(enabled);
		attachmentSubjectField.setEnabled(enabled);
		addAttachmentContentButton.setEnabled(enabled);
		btnAddAttachment.setEnabled(enabled);
	}

	private void clearAttachmentFields() {
		attachmentContentField.setText("");
		attachmentSubjectField.setText("");
	}

	private void enableDigipostAddressFields() {
		enableFields(true, false, false);
	}

	private void enablePersonalIdentificationNumberFields() {
		enableFields(false, true, false);
	}

	private void enableNameAndAddressFields() {
		enableFields(false, false, true);
	}

	private void enableFields(final boolean digipostAddress, final boolean personalIdentificationNumber, final boolean nameAndAddress) {
		recipientDigipostAddressField.setEnabled(digipostAddress);
		recipientPersonalIdentificationNumberField.setEnabled(personalIdentificationNumber);
		recipientNameField.setEnabled(nameAndAddress);
		recipientAddress1Field.setEnabled(nameAndAddress);
		recipientAddress2Field.setEnabled(nameAndAddress);
		recipientPostalcodeField.setEnabled(nameAndAddress);
		recipientCityField.setEnabled(nameAndAddress);
		recipientBirthDateField.setEnabled(nameAndAddress);
		recipientPhoneNumberField.setEnabled(nameAndAddress);
		recipientEmailAddressField.setEnabled(nameAndAddress);
		fallbackToPrintCheckBox.setEnabled(nameAndAddress);
		directToPrintCheckBox.setEnabled(nameAndAddress);
	}

	private GridBagConstraints createGridBagConstraintsForField(final int gridx, final int gridy) {
		return createGridBagConstraintsForField(gridx, gridy, 3);
	}

	private GridBagConstraints createGridBagConstraintsForField(final int gridx, final int gridy, final int gridwidth) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = gridwidth;
		return gbc;
	}

	private GridBagConstraints createGridBagConstraintsForRadioButton(final int gridx, final int gridy) {
		return createGridBagConstraints(GridBagConstraints.WEST, gridx, gridy);
	}

	private GridBagConstraints createGridBagConstraintsForLabel(final int gridx, final int gridy) {
		return createGridBagConstraints(GridBagConstraints.EAST, gridx, gridy);
	}

	private GridBagConstraints createGridBagConstraints(final int anchor, final int gridx, final int gridy) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = anchor;
		gbc.insets = new Insets(0, 0, 5, 5);
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		return gbc;
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
