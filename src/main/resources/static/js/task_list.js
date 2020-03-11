
//idに紐づく要素をゲット
var showAll = document.getElementById('showAll');
var showNoTouch = document.getElementById('showNoTouch');
var showProceeding = document.getElementById('showProceeding');
var showCompleted = document.getElementById('showCompleted');
//var createTablelistChecker = document.getElementById('createTablelistChecker');

//イベントリスナーを仕掛ける
showAll.addEventListener('click', function () {
    showRows("showAll");
})
showNoTouch.addEventListener('click', function () {
    showRows("未着手");
})
showProceeding.addEventListener('click', function () {
    showRows("進行中");
})
showCompleted.addEventListener('click', function () {
    showRows("処理済み");
})
//createTablelistChecker.addEventListener('DOMContentLoaded', function () {
//	checker();
//})

//ステータスによって表示を切り替える
function showRows(target) {
    //ターゲットを取得
    var tgt = target;
    //変数を決める
    var tables = document.getElementsByClassName("tbl");
    var tableCnt = tables.length;
    var FLG = document.getElementByName
    var ttr = "";
    var ttd = "";
    //デバッグ
    console.log("tables");

    //テーブル数の数だけ処理を繰り返す
    for (i = 0; i < tableCnt; i++) {
        var table = tables[i];
        var rowCnt = table.rows.length;
        for (j = 0; j < rowCnt; j++) {
            ttr = table.rows[j];
            ttd = ttr.cells[4];
            if (tgt === "showAll" || !(ttd.textContent != tgt && ttd.textContent != "進捗状況")) {
                ttr.style.display = "";
            } else {
                ttr.style.display = "none";
            }
        }
    }
}

//テーブルカラムがある場合はタスク一覧作成ボタン非表示
window.onload = function () {
	    //テーブルを取得
	    var tables = document.getElementsByClassName("tbl");
	    //テーブル数
	    var tableCnt = tables.length;
	    //カウンタ
	    var counter=0;

	    //テーブル数の数だけ処理を繰り返す
	    for (i = 0; i < tableCnt; i++) {
	        var table = tables[i];
	        var rowCnt = table.rows.length;
	        counter += rowCnt;
	    }
	    
	    if (counter != tableCnt) {
	    	createTablelistChecker.style.display = "none";
	    }
}








