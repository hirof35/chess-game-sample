Java AI Chess Project
JavaFXを使用して開発された、AI対戦機能付きのチェスアプリケーションです。

🚀 主な機能
GUI対局: マウスクリックによる直感的な駒の操作（2段階クリック選択方式）。

個別駒描画: 各駒を独立した画像ファイルから読み込み、高精細に表示。

AI対戦エンジン:

Minimax法 / Alpha-Beta枝刈り: 数手先を読み、最適な一手を計算。

難易度設定: 探索の深さを調整することで「初級・中級・上級」の切り替えが可能。

チェス・ロジック:

全駒（ポーン、ルーク、ナイト、ビショップ、クイーン、キング）の基本移動アルゴリズムの実装。

移動経路の遮蔽物判定（isPathClear）。

ゲーム体験演出:

選択中のマスのハイライト表示。

対局決着時のアニメーション演出。

「待った（Undo）」機能による手戻り操作。

🛠 使用技術
言語: Java 25 (Java SE 21以降対応)

ライブラリ: JavaFX 25 (GUI)

アルゴリズム: Minimax Algorithm with Alpha-Beta Pruning

開発環境: Eclipse IDE

📂 構成とセットアップ
画像リソースの配置
/src/res/ フォルダ内に以下の形式で画像を配置する必要があります。

白駒: white_pawn.png, white_rook.png ...

黒駒: black_pawn.png, black_rook.png ...
※現在は .jpg 形式の個別ファイル（「ポーン1.jpg」など）にも対応。

実行方法
JavaFX SDKをプロジェクトに追加。

res フォルダを Source Folder に設定。

VM引数に以下を追加して実行：
--module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics

📝 開発のこだわり
非同期処理 (Task): AIの思考中にGUIがフリーズしないよう、マルチスレッド処理を導入。

スプライトから個別描画への転換: 柔軟な拡大縮小とメンテナンス性を重視し、画像管理を刷新。

評価関数の自作: 駒の価値だけでなく、配置の有利不利（PST）を考慮した思考ロジックを検討。

READMEに「今後の課題」として書けること
もしさらに発展させるなら、以下を「To-Do」として載せると技術力がよりアピールできます。

[ ] キャスリング・アンパッサン等の特殊ルールの完全実装。

[ ] ネットワーク対戦機能の追加。

[ ] 棋譜（PGN形式）の保存・読み込み機能。

<img width="865" height="1036" alt="スクリーンショット 2026-05-07 125111" src="https://github.com/user-attachments/assets/61aef4b0-91da-4979-833a-c9b2d0f0b6f4" />
