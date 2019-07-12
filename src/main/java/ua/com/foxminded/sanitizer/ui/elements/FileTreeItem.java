package ua.com.foxminded.sanitizer.ui.elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.NoArgsConstructor;
import ua.com.foxminded.sanitizer.data.Config;
import ua.com.foxminded.sanitizer.data.FileData;
import ua.com.foxminded.sanitizer.ui.FileView;
import ua.com.foxminded.sanitizer.worker.FileWorker;

@NoArgsConstructor
public class FileTreeItem extends TreeItem<File> {
    public class CustomFileTreeCell extends TextFieldTreeCell<File> {
        @Override
        public void updateItem(File item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                if (item.isFile()) {
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem mi1 = new MenuItem("View " + item.getName());
                    mi1.setOnAction(event -> {
                        new FileView(item.toString()).show();
                    });
                    contextMenu.getItems().add(mi1);
                    setContextMenu(contextMenu);
                }
                setEditable(false);
                this.setText(item.getName());
            }
        }
    }

    private boolean isFirstTimeChildren = true;
    private boolean isFirstTimeLeaf = true;
    private boolean isLeaf;
    private Image folderCollapsedImage = new Image(getClass().getResourceAsStream("/img/folder.png"));
    private Image folderOpenedImage = new Image(getClass().getResourceAsStream("/img/folder_open.png"));
    private Image fileImage = new Image(getClass().getResourceAsStream("/img/file.png"));
    private ObservableList<FileData> dataView;
    private ArrayList<FileData> fileList = new ArrayList<FileData>();
    private TableView<FileData> tableView = new TableView<FileData>();
    // private String extension = ".java"; // not constant, could be a list
    private Config config = new Config();
    private FileWorker fileWorker = new FileWorker();

    public FileTreeItem(File file, Config config) {
        super(file);
        this.config = config;
        setGraphic(file.isDirectory() ? new ImageView(folderCollapsedImage) : new ImageView(fileImage));

        addEventHandler(TreeItem.branchExpandedEvent(), event -> {
            TreeItem<Object> source = event.getSource();
            if (source.isExpanded()) {
                ImageView iv = (ImageView) source.getGraphic();
                iv.setImage(folderOpenedImage);
                try {
                    processDirectory(Paths.get(source.getValue().toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        addEventHandler(TreeItem.branchCollapsedEvent(), event -> {
            TreeItem<Object> source = event.getSource();
            if (!source.isExpanded()) {
                ImageView iv = (ImageView) source.getGraphic();
                iv.setImage(folderCollapsedImage);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public TableView<FileData> getTableView() {
        dataView = FXCollections.observableArrayList(fileList);

        TableColumn<FileData, String> indexCol = new TableColumn<FileData, String>("#");
        indexCol.setCellFactory(column -> new TableCell<FileData, String>() {
            @Override
            public void updateIndex(int index) {
                super.updateIndex(index);
                setText((isEmpty() || index < 0) ? null : Integer.toString(index + 1));
            }
        });

        TableColumn<FileData, String> filenameCol = new TableColumn<FileData, String>("File");
        filenameCol.setCellValueFactory(new PropertyValueFactory<FileData, String>("fileName"));

        tableView.setRowFactory(tv -> {
            final TableRow<FileData> row = new TableRow<>();
            final MenuItem mi1 = new MenuItem();
            mi1.setOnAction(event -> new FileView(row.getItem().getFileName()).show());

            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    mi1.setText("View " + Paths.get(row.getItem().getFileName()).getFileName());
                }
            });
            row.emptyProperty().addListener(
                    (observable, wasEmpty, isEmpty) -> row.setContextMenu(isEmpty ? null : new ContextMenu(mi1)));
            return row;
        });
        tableView.setItems(dataView);
        tableView.getColumns().addAll(indexCol, filenameCol);
        tableView.getSortOrder().add(indexCol);
        tableView.getSortOrder().add(filenameCol);
        indexCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.10));
        indexCol.setResizable(true);
        filenameCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.90));
        filenameCol.setResizable(true);
        tableView.setPlaceholder(new Label("no proper files here"));
        return tableView;
    }

    @Override
    public ObservableList<TreeItem<File>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;
            super.getChildren().setAll(buildChildren(this));
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeaf) {
            isFirstTimeLeaf = false;
            File file = (File) getValue();
            isLeaf = file.isFile();
        }
        return isLeaf;
    }

    private ObservableList<TreeItem<File>> buildChildren(TreeItem<File> TreeItem) {
        File file = TreeItem.getValue();
        if (file != null && file.isDirectory()) {
            // with FileFilter
            File[] files = file.listFiles(pathname -> {
                boolean isShownDirectory = (!pathname.isHidden()) && pathname.isDirectory();
                boolean isShownFile = (!pathname.isHidden()) && (!pathname.isDirectory())
                // && pathname.getName().toLowerCase().endsWith(extension);
                        && fileWorker.isMatchPatterns(file, config);
                return isShownDirectory || isShownFile;
            });

            if (files != null) {
                ObservableList<TreeItem<File>> children = FXCollections.observableArrayList();
                for (File childFile : files) {
                    children.add(new FileTreeItem(childFile, config));
                }
                return children;
            }
        }
        return FXCollections.emptyObservableList();
    }

    private void processDirectory(Path dir) throws IOException {
        fileList.clear();
        File[] files = dir.toFile().listFiles(pathname -> {
            boolean isShownFile = (!pathname.isHidden()) && (!pathname.isDirectory())
            // && pathname.getName().toLowerCase().endsWith(extension);
                    && fileWorker.isMatchPatterns(pathname, config);
            return isShownFile;
        });

        for (File F : files) {
            FileData tableItem = new FileData();
            tableItem.setFileName(F.getAbsolutePath());
            fileList.add(tableItem);
        }
        dataView = FXCollections.observableArrayList(fileList);
        tableView.setItems(dataView);
    }
}
