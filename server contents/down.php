<?php
$filename = "furniture.jpg"; 
$file = $_SERVER['DOCUMENT_ROOT'] . "./" . $filename;
 
/*
 *  ex)   $filename = "image1.png"; 
 *        $file =  $_SERVER['DOCUMENT_ROOT'] . "/images/" .$filename;
 */
 
if (is_file($file)) {
 
    if (preg_match("MSIE", $_SERVER['HTTP_USER_AGENT'])) { 
        header("Content-type: application/octet-stream"); 
        header("Content-Length: ".filesize("$file"));
        header("Content-Disposition: attachment; filename=$filename"); // 다운로드되는 파일명 (실제 파일명과 별개로 지정 가능)
        header("Content-Transfer-Encoding: binary"); 
        header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
        header("Pragma: public"); 
        header("Expires: 0"); 
    }
    else { 
        header("Content-type: file/unknown"); 
        header("Content-Length: ".filesize("$file")); 
        header("Content-Disposition: attachment; filename=$filename"); // 다운로드되는 파일명 (실제 파일명과 별개로 지정 가능)
        header("Content-Description: PHP3 Generated Data"); 
        header("Pragma: no-cache"); 
        header("Expires: 0"); 
    }
 
    $fp = fopen($file, "rb"); 
    fpassthru($fp);
    fclose($fp);
}
else {
    echo "해당 파일이 없습니다.";
}
?>
