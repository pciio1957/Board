package board.util;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class A01_DownLoadCtrl {
	
	// http://localhost:7080/board/download.do?fname=@@ 로 처리가능
	@RequestMapping("download")
	public String download(@RequestParam("fname") String fname, Model d) {
		
		// 모델을 이용해서 실제 파일이름을 넘김
		// Viewer안에 있는 모델명과 동일하게 하여 파일명 전달
		// Viewer -> util 폴더에 있는 DownloadViewer 클래스임
		d.addAttribute("downloadFile", fname);
		return "downloadViewer";
	}
}
