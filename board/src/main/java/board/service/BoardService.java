package board.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import board.dao.BoardDao;
import board.vo.Board;
import board.vo.BoardFile;
import board.vo.BoardSch;

@Service
public class BoardService {
	// mybatis에서 만들어진 BoardDao를 상속받은 실제객체가 container에 메모리가 있으면
	// 할당처리..
	// 객체가 없더라도 에러가 발생하지 않게 처리..
	@Autowired(required = false)
	private BoardDao dao;
	public ArrayList<Board> boardList(BoardSch sch){
		if(sch.getSubject()==null) sch.setSubject("");
		if(sch.getWriter()==null) sch.setWriter("");
		
		// 1006 페이징 추가
		// 1. 데이터의 총 건수 설정
		sch.setCount(dao.totCnt(sch));
		
		// 2. 페이지크기(요청) : default로 페이지 크기 지정 - 초기화면
		if(sch.getPageSize() == 0) {
			sch.setPageSize(5);
		}
		
		// 3. 총 페이지 수
		//	ex) 데이터 건수가 10건일때는 2페이지
		//		데이터 건수가 16건일때 3페이지와 남은 1개를 나타내기 위해 4페이지
		//	sch.getCount()/sch.getPageSize()로 한 다음
		//	Math.ceil() : 올림처리를 하면 나머지값이 존재하면 올림 처리를 함 
		//	문제점1: java에서는 정수/정수로 나누면 정수로 출력됨
		//	원하는 대로 소숫점 처리를 하려면 double로 promote(형변환) 필요
		//	문제점2 : Math.ceil()의 리턴값은 실수이므로 정수로 casting해야함
		//int totPage = (int)Math.ceil(sch.getCount()/(double) sch.getPageSize());
		sch.setPageCount((int)Math.ceil(sch.getCount()/(double) sch.getPageSize()));
		
		// 4. 화면에서 클릭한 페이지 번호 (요청값) : 초기화면을 위한 default 설정
		if(sch.getCurPage() == 0) {
			sch.setCurPage(1);
		}
		
		// 현재 페이지는 총 페이지수를 넘지 못한다 
		if(sch.getCurPage()>sch.getPageCount()) {
			sch.setCurPage(sch.getPageCount());
		}
		
		// 5. DB를 이용하여 한페이지에 보일 데이터 선언(시작번호/마지막번호 지정)
		//	sch.getCurPage() : 클릭한 페이지번호
		//	sch.getPageSize() : 한번에 보여줄 데이터건수
		//	클릭한 페이지가 1이고 한번에 보여줄 데이터가 5이면 -> 마지막 페이지 번호는 1*5= 5페이지
		//	클릭한 페이지가 2이고 한번에 보여줄 데이터가 5이면 -> 마지막 페이지 번호는 1*5= 10페이지
		sch.setStart((sch.getCurPage()-1)*sch.getPageSize()+1);
		sch.setEnd(sch.getCurPage() * sch.getPageSize());
		
		/*
		
		마지막번호는 페이지번호와 한페이지에 보이는 데이터 건수를 곱하는 것으로 쉬움
		시작번호는 마지막번호의 규칙에서 작성해야함 
		
		한 페이지당 5건씩 보이는 페이지에서 확인
		페이지번호		페이지건수 		보일페이지				시작페이지
		1			5			1  2  3  4  5		(1-1)*5+1
		2			5			6  7  8  9  10		(2-1)*5+1
		3 			5			11 12 13 14 15		(3-1)*5+1
		4			5			16 17 18 19 20		(4-1)*15+1
		시작페이지의 경우 (페이지번호에서-1) * (페이지 총건수+1) 로 구한다
		
		*/
		
		
		// 블럭 처리 
		// 1. 블럭 크기 지정
		sch.setBlockSize(5);
		
		// 2. blocknum 지정 : 전체 페이지 번호/블럭 크기
		int blocknum = (int) (Math.ceil(sch.getCurPage()/(double)sch.getBlockSize()));
		
		// 3. 마지막 블럭 번호 
		//	마지막 블럭번호가 딱 떨어지는 경우가 잘 없음 
		// 	총 페이지수가 23일때 -> 한번에 보여줄 block의 수가 5로 25가 나오므로 에러 발생
		// 	그래서 마지막 블럭번호는 총 페이지수를 넘지 못하게 처리해야함
		int endblock = blocknum * sch.getBlockSize();
		if(endblock>sch.getPageCount()) {
			endblock = sch.getPageCount();
		}
		sch.setEndBlock(endblock);
		sch.setStartBlock((blocknum-1) * sch.getBlockSize()+1);
		
		return dao.boardList(sch);
	}
	public void insertBoard(Board ins) {
		System.out.println("#첨부파일#" + ins.getReport().getOriginalFilename());
		dao.insertBoard(ins);
		uploadFile(ins.getNo(), ins.getReport());
	}
	
	// info에 저장한 값들 가져오기 
	@Value("${upload}") private String upload;
	@Value("${tmpUpload}") private String tmpUpload;
	
	public void uploadFile(int no, MultipartFile report) {
		// 1. multipartfile 객체를 file객체로 변환
		String fileName = report.getOriginalFilename();
		if(fileName != null && !fileName.equals("")) {
			File tmpFile = new File(tmpUpload + fileName);
			File orgFile = new File(upload + fileName);
			try {
				// 자동 예외처리 선언, 파일로 변환 
				report.transferTo(tmpFile);
				// 최종 웹 서버에 있는 파일 위치로 로딩
				Files.copy(tmpFile.toPath(), orgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				
				
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// 2. DB처리하기
		dao.uploadFile(new BoardFile(fileName));
	}
	public Board getBoard(int no) {
		// 상세조회시 조회카운트 up 
		dao.uptReadCnt(no);
		// 기본 내용은 board이지만 파일을 가져올 수 있도록 파일이름 연결
		Board b = dao.getBoard(no);
		b.setFname(dao.getBoadFile(no));
		return b;
	}	
	public void update(Board update) {
		dao.updateBoard(update);
	}	
	public void deleteBoard(int no) {
		dao.deleteBoard(no);
	}	
}
