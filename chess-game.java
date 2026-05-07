package chess_game;

import java.util.Stack;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class chess extends Application {

    private Stage stage;
    private StackPane root;
    private GridPane boardGui;
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean isAiThinking = false;
    private Image spriteSheet;

    private int[][] board = new int[8][8];
    private Stack<int[][]> history = new Stack<>();
    private int selectedX = -1;
    private int selectedY = -1;
    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        this.root = new StackPane();
        
        // タイトル画面を表示
        showTitleScreen();

        Scene scene = new Scene(root, 700, 800);
        primaryStage.setTitle("Java Chess - Individual Pieces");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showTitleScreen() {
        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2c3e50;");

        Label title = new Label("JAVA CHESS AI");
        title.setStyle("-fx-font-size: 50; -fx-text-fill: white; -fx-font-weight: bold;");

        ComboBox<Difficulty> diffBox = new ComboBox<>();
        diffBox.getItems().addAll(Difficulty.values());
        diffBox.setValue(Difficulty.NORMAL);
        diffBox.setOnAction(e -> difficulty = diffBox.getValue());

        Button startBtn = new Button("対局開始");
        startBtn.setPrefSize(200, 50);
        startBtn.setOnAction(e -> initGame());

        layout.getChildren().addAll(title, new Label("難易度を選択:"), diffBox, startBtn);
        root.getChildren().setAll(layout);
    }

    private void initGame() {
        board = new int[8][8];
        // 盤面初期化 (正:白, 負:黒)
        int[] blackRow = {-3, -4, -2, -1, -2, -2, -4, -3}; // R, N, B, Q, K...
        int[] whiteRow = {3, 4, 2, 1, 2, 2, 4, 3}; 
        
        for(int i=0; i<8; i++) {
            board[0][i] = blackRow[i];
            board[1][i] = -5; // 黒ポーン
            board[6][i] = 5;  // 白ポーン
            board[7][i] = whiteRow[i];
        }
        
        boardGui = new GridPane();
        boardGui.setAlignment(Pos.CENTER);
        refreshBoard();

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        Button undoBtn = new Button("待った (Undo)");
        undoBtn.setOnAction(e -> handleUndo());
        controls.getChildren().add(undoBtn);

        VBox gameLayout = new VBox(20, boardGui, controls);
        gameLayout.setAlignment(Pos.CENTER);
        root.getChildren().setAll(gameLayout);
    }

    
    private void refreshBoard() {
        boardGui.getChildren().clear();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                StackPane tile = new StackPane();
                tile.setMinSize(75, 75);
                
                // 基本の色（緑と白）
                String baseColor = ((r + c) % 2 == 0 ? "#eeeed2" : "#769656");
                
                // 【重要】選択されているマスだけ色を変える
                if (c == selectedX && r == selectedY) {
                    tile.setStyle("-fx-background-color: #f6f669;"); // 選択中（明るい黄色）
                } else {
                    tile.setStyle("-fx-background-color: " + baseColor + ";");
                }

                if (board[r][c] != 0) {
                    ImageView iv = getPieceImageView(board[r][c]);
                    if (iv != null) tile.getChildren().add(iv);
                }

                final int col = c;
                final int row = r;
                tile.setOnMouseClicked(e -> handlePlayerMove(col, row));
                boardGui.add(tile, c, r);
            }
        }
    }
    
    private ImageView getPieceImageView(int pieceCode) {
        String fileName = getFileNameByCode(pieceCode);
        try {
            // 画像の読み込み
            Image img = new Image(getClass().getResourceAsStream(fileName));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(70);
            iv.setFitHeight(70);
            iv.setPreserveRatio(true);
            iv.setEffect(new DropShadow(5, Color.BLACK));
            return iv;
        } catch (Exception e) {
            System.out.println("画像が見つかりません: " + fileName);
            return null;
        }
    }

    private String getFileNameByCode(int pieceCode) {
        switch (pieceCode) {
            case 5:  return "ポーン1.jpg";
            case 4:  return "ナイト1.jpg";
            case 3:  return "ルーク1.jpg";
            case 2:  return "キング1.jpg";
            case 1:  return "クイーン1.jpg";
            case -5: return "ポーン2.jpg";
            case -4: return "ナイト2.jpg";
            case -3: return "ルーク2.jpg";
            case -2: return "キング2.jpg";
            case -1: return "クイーン2.jpg";
            default: return "ポーン1.jpg"; 
        }
    }
    private void executeMove(int fx, int fy, int tx, int ty) {
        // 1. 移動前の盤面をスタックに保存（Undo用）
        history.push(copyBoard(board));

        // 2. 移動先に敵の駒がいたら「取る」処理（データ上は上書きするだけ）
        // ※もし「取った駒一覧」を表示したい場合は、ここで board[ty][tx] をリストに保存します。

        // 3. 駒を移動させる
        board[ty][tx] = board[fy][fx]; // 移動先に元の駒を置く
        board[fy][fx] = 0;             // 元の場所を空にする
        
        // 4. コンソールにログを出力（デバッグ用）
        System.out.println("Move: (" + fx + "," + fy + ") to (" + tx + "," + ty + ")");
    }
    private void handlePlayerMove(int x, int y) {
        if (isAiThinking) return;

        if (selectedX == -1) {
            // 最初のクリック：自分の駒を選択
            if (board[y][x] > 0) {
                selectedX = x;
                selectedY = y;
                refreshBoard(); // 色を変えるために再描画
            }
        } else {
            // 2回目のクリック：移動先を選択
            if (isValidMove(selectedX, selectedY, x, y)) {
                executeMove(selectedX, selectedY, x, y);
                selectedX = -1;
                selectedY = -1;
                refreshBoard();
                startAiTurn();
            } else {
                // 移動できない場所か、自分の別の駒を選び直した場合
                if (board[y][x] > 0) {
                    selectedX = x;
                    selectedY = y;
                } else {
                    selectedX = -1;
                    selectedY = -1;
                }
                refreshBoard();
            }
        }
    }
    private void startAiTurn() {
        isAiThinking = true;
        boardGui.setDisable(true);
        Task<int[]> aiTask = new Task<>() {
            @Override
            protected int[] call() throws Exception {
                Thread.sleep(1000); 
                for (int r=1; r<7; r++) {
                    for (int c=0; c<8; c++) {
                        if (board[r][c] == -5 && board[r+1][c] == 0) {
                            return new int[]{c, r, c, r+1};
                        }
                    }
                }
                return null;
            }
        };
        aiTask.setOnSucceeded(e -> {
            int[] move = aiTask.getValue();
            if (move != null) {
                board[move[3]][move[2]] = board[move[1]][move[0]];
                board[move[1]][move[0]] = 0;
                refreshBoard();
            }
            isAiThinking = false;
            boardGui.setDisable(false);
        });
        new Thread(aiTask).start();
    }

    private void handleUndo() {
        if (!history.isEmpty()) {
            board = history.pop();
            refreshBoard();
        }
    }

    private int[][] copyBoard(int[][] source) {
        int[][] dest = new int[8][8];
        for (int i = 0; i < 8; i++) System.arraycopy(source[i], 0, dest[i], 0, 8);
        return dest;
    }

    enum Difficulty {
        EASY(2), NORMAL(4), HARD(6);
        private int depth;
        Difficulty(int d) { this.depth = d; }
    }
    private boolean isValidMove(int fx, int fy, int tx, int ty) {
        int piece = Math.abs(board[fy][fx]);
        int dx = Math.abs(tx - fx);
        int dy = Math.abs(ty - fy);

        switch (piece) {
            case 5: // ポーン
                // 白は上に、黒は下に進む。1歩または2歩（初期位置）。
                return (fx == tx && board[ty][tx] == 0); 
            case 3: // ルーク (直線: 横か縦)
                return (fx == tx || fy == ty) && isPathClear(fx, fy, tx, ty);
            case 4: // ナイト (L字: 1-2 または 2-1)
                return (dx == 1 && dy == 2) || (dx == 2 && dy == 1);
            case 2: // ビショップ (斜め: dx == dy)
                return (dx == dy) && isPathClear(fx, fy, tx, ty);
            case 1: // クイーン (ルーク + ビショップ)
                return (fx == tx || fy == ty || dx == dy) && isPathClear(fx, fy, tx, ty);
            case 6: // キング (周囲1マス)
                return dx <= 1 && dy <= 1;
            default:
                return false;
        }
    }
    private boolean isPathClear(int fx, int fy, int tx, int ty) {
        int xDir = Integer.compare(tx, fx); // 右なら1, 左なら-1, 動かないなら0
        int yDir = Integer.compare(ty, fy); // 下なら1, 上なら-1, 動かないなら0

        int x = fx + xDir;
        int y = fy + yDir;

        // 目的地の一つ手前までループで確認
        while (x != tx || y != ty) {
            if (board[y][x] != 0) {
                return false; // 途中に何か駒があれば「壁」となり移動不可
            }
            x += xDir;
            y += yDir;
        }
        return true;
    }
    

    
    public static void main(String[] args) { launch(args); }
}
