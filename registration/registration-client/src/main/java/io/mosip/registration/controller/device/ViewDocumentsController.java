package io.mosip.registration.controller.device;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.api.docscanner.DocScannerFacade;
import io.mosip.registration.api.docscanner.DocScannerUtil;
import io.mosip.registration.api.docscanner.dto.DocScanDevice;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.context.SessionContext;
import io.mosip.registration.controller.BaseController;
import io.mosip.registration.controller.reg.DocumentScanController;
import io.mosip.registration.util.common.RectangleSelection;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

@Controller
public class ViewDocumentsController extends BaseController implements Initializable {
	private static final Logger LOGGER = AppConfig.getLogger(ViewDocumentsController.class);

	@FXML
	protected Label docPreviewNext;
	@FXML
	protected Label docPreviewPrev;
	@FXML
	protected Text docCurrentPageNumber;
	@FXML
	protected GridPane previewOption;
	@FXML
	private Button captureBtn;
	@FXML
	private Button cancelBtn;
	@FXML
	private Button cropButton;
	@FXML
	private Button streamBtn;
	@FXML
	private Button previewBtn;
	@FXML
	private GridPane imageViewGridPane;
	@FXML
	private ImageView scanImage;
	/*@FXML
	private ImageView closeImageView;*/
	@FXML
	private ImageView streamImageView;
	@FXML
	private ImageView captureImageView;
	@FXML
	private ImageView saveImageView;
	@FXML
	private ImageView backImageView1;
	@FXML
	private ImageView cancelImageView;	
	@FXML
	private ImageView previewImageView;
	@FXML
	private Group imageGroup;
	@FXML
	private StackPane groupStackPane;
	@FXML
	private ScrollPane docPreviewScrollPane;

	@FXML
	private TabPane documentsViewTabPane;

	@Autowired
	private BaseController baseController;
	@Autowired
	private Streamer streamer;
	@Autowired
	private DocumentScanController documentScanController;
	@Autowired
	private DocScannerFacade docScannerFacade;

	private Thread streamer_thread = null;
	private Stage popupStage;
	public TextField streamerValue;
	private boolean isWebCamStream;
	private boolean isStreamPaused;
	public DocScanDevice docScanDevice;
	private RectangleSelection rectangleSelection = null;
	final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

	/**
	 * @return the popupStage
	 */
	public Stage getPopupStage() {
		return popupStage;
	}

	/**
	 * @param popupStage the popupStage to set
	 */
	public void setPopupStage(Stage popupStage) {
		this.popupStage = popupStage;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	private void initializeDocPages(int currentPage, int totalPages) {
//		docCurrentPageNumber.setText(String.valueOf(currentPage));
//		totalScannedPages.setText(String.valueOf(totalPages));
//		boolean prevPageDisable = currentPage > 1 ? false : true;
//		docPreviewPrev.setDisable(prevPageDisable);
//		boolean nextPageDisable = currentPage < totalPages ? false : true;
//		docPreviewNext.setDisable(nextPageDisable);
	}

	/**
	 * This method will open popup to scan
	 * 
	 * @param parentControllerObj
	 * @param title
	 */
	public void init(BaseController parentControllerObj, String title, Map<String, String> documemnts) {
		try {
			streamerValue = new TextField();
			baseController = parentControllerObj;

			LOGGER.info("Loading Document scan page : {}", RegistrationConstants.VIEW_DOCUMENTS_PAGE);
			Parent scanPopup = BaseController.load(getClass().getResource(RegistrationConstants.VIEW_DOCUMENTS_PAGE));

			Scene scene = new Scene(scanPopup, 200, 400);
			scene.getStylesheets().add(ClassLoader.getSystemClassLoader().getResource(getCssName()).toExternalForm());
			popupStage = new Stage();
			//popupStage.setResizable(true);
			//popupStage.setFullScreen(true);
			popupStage.setAlwaysOnTop(true);
			//popupStage.initStyle(StageStyle.UNDECORATED);
			popupStage.setScene(scene);
			popupStage.initModality(Modality.WINDOW_MODAL);
			popupStage.initOwner(fXComponents.getStage());
			popupStage.setTitle(title);
			popupStage.setMinHeight(docScanDevice.getHeight());
			popupStage.setMinWidth(docScanDevice.getWidth());
			createTabs(documemnts);
			popupStage.show();

			LOGGER.debug("documents screen launched");

			rectangleSelection = null;

			LOGGER.info("Opening pop-up screen to scan for user registration");

		} catch (IOException exception) {
			LOGGER.error(RegistrationConstants.USER_REG_SCAN_EXP, exception);
			generateAlert(RegistrationConstants.ERROR, RegistrationUIConstants.getMessageLanguageSpecific(RegistrationUIConstants.UNABLE_LOAD_SCAN_POPUP));
		}
	}

	public Text getScanningMsg() {
		return scanningMsg;
	}

	public void setScanningMsg(String msg) {
		if (scanningMsg != null) {
			scanningMsg.setText(msg);
			scanningMsg.getStyleClass().add("scanButton");
		}
	}

	private GridPane getScreenGridPane(String screenName) {
		GridPane gridPane = new GridPane();
		gridPane.setId(screenName);
		RowConstraints topRowConstraints = new RowConstraints();
		topRowConstraints.setPercentHeight(2);
		RowConstraints midRowConstraints = new RowConstraints();
		midRowConstraints.setPercentHeight(96);
		RowConstraints bottomRowConstraints = new RowConstraints();
		bottomRowConstraints.setPercentHeight(2);
		gridPane.getRowConstraints().addAll(topRowConstraints,midRowConstraints, bottomRowConstraints);

		ColumnConstraints columnConstraint1 = new ColumnConstraints();
		columnConstraint1.setPercentWidth(5);
		ColumnConstraints columnConstraint2 = new ColumnConstraints();
		columnConstraint2.setPercentWidth(90);
		ColumnConstraints columnConstraint3 = new ColumnConstraints();
		columnConstraint3.setPercentWidth(5);

		gridPane.getColumnConstraints().addAll(columnConstraint1, columnConstraint2,
				columnConstraint3);

		return gridPane;
	}

	private void createTabs(Map<String,String> docs){
		docs.forEach((name,doc)->{
			Tab docTab = new Tab();
			docTab.setId(name);
			docTab.setText(name);

			GridPane tabGridPane = getScreenGridPane(name);
			tabGridPane.prefWidthProperty().bind(documentsViewTabPane.widthProperty());
			tabGridPane.prefHeightProperty().bind(documentsViewTabPane.heightProperty());
			tabGridPane.setStyle("-fx-background-color: white;");

//			add document view to the tab grid
			final ScrollPane scrollPane = new ScrollPane(tabGridPane);
			scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			scrollPane.setId("scrollPane");
			docTab.setContent(scrollPane);
			documentsViewTabPane.getTabs().add(docTab);

		});
	}
}
