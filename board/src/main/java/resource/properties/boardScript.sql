
CREATE TABLE board (
	no NUMBER PRIMARY KEY,
	refno NUMBER, -- 답변의 상위 글번호
	subject varchar2(200),
	content varchar2(2000),
	writer varchar2(100),
	readcnt NUMBER, -- 조회수
	regdte DATE, -- 등록일
	uptdte DATE -- 수정일
);

CREATE SEQUENCE board_seq
	START WITH 1
	MINVALUE 1
	INCREMENT BY 1
	MAXVALUE 999999;
	

-- 기본 등록 sql
INSERT INTO board values(board_seq.nextval, 0, '첫번째글', '내용', '홍길동', 0, sysdate, sysdate);

SELECT * FROM board;
SELECT * FROM board WHERE NO = 1;

CREATE TABLE boardfile(
	NO NUMBER, -- 글번호
	fname varchar2(200),
	pathname varchar2(500),
	content varchar2(200),
	credte DATE 
);

-- 답글
UPDATE board 
	SET readcnt = readcnt + 1
WHERE NO = 1;

/*
UPDATE board 
	SET readcnt = readcnt + 1;
WHERE NO = #{no};
*/

-- 1) 계층형 데이터를 나타낼 페이지수를 지정 (시작과 끝)
SELECT *
FROM (
	SELECT rownum cnt, b.* 
	FROM board b
	WHERE subject LIKE '%'||''||'%'
	AND writer LIKE '%'||''||'%'
	START WITH refno = 0
	CONNECT BY PRIOR NO = refno
	ORDER siblings BY NO DESC)
WHERE cnt BETWEEN 1 AND 5;


-- 2) 해당되는 데이터의 총 건수를 구함 (계층형관련 데이터는 굳이 필요하지않아 삭제)
SELECT count(*) 
FROM board b
WHERE subject LIKE '%'||''||'%'
AND writer LIKE '%'||''||'%';


