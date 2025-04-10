package com.example.Flicktionary.domain.post.service

import com.example.Flicktionary.domain.post.dto.PostCreateRequestDto
import com.example.Flicktionary.domain.post.dto.PostResponseDto
import com.example.Flicktionary.domain.post.dto.PostUpdateRequestDto
import com.example.Flicktionary.domain.post.entity.Post
import com.example.Flicktionary.domain.post.repository.PostRepository
import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.entity.UserAccountType
import com.example.Flicktionary.domain.user.repository.UserAccountRepository
import com.example.Flicktionary.global.exception.ServiceException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import java.time.LocalDateTime
import java.util.*

@DisplayName("게시글 서비스 테스트")
@ExtendWith(MockitoExtension::class)
class PostServiceTest {

    @InjectMocks
    private lateinit var postService: PostService

    @Mock
    private lateinit var postRepository: PostRepository

    @Mock
    private lateinit var userAccountRepository: UserAccountRepository

    /* 테스트용 변수 설정 */
    // 테스트용 유저
    private lateinit var testUser: UserAccount

    // 테스트용 요청 게시글 Dto
    private lateinit var requestDto: PostCreateRequestDto

    // 테스트용 저장된 게시글 엔티티
    private lateinit var savedPost: Post

    // 검증용 게시글 응답 Dto
    private lateinit var expectedPost: PostResponseDto

    @BeforeEach
    fun setUp() {
        testUser = UserAccount(
            1L,
            "테스트용 유저",
            "test12345",
            "test@email.com",
            "테스트 유저",
            UserAccountType.USER
        )

        requestDto = PostCreateRequestDto(
            userAccountId = testUser.id,
            title = "테스트 제목",
            content = "테스트 내용",
            isSpoiler = false
        )

        savedPost = Post(
            id = 1L,
            userAccount = testUser,
            title = "테스트 제목",
            content = "테스트 내용",
            isSpoiler = false,
            createdAt = LocalDateTime.now()
        )

        expectedPost = PostResponseDto(
            id = 1L,
            userAccountId = testUser.id,
            nickname = "테스트 유저",
            title = "테스트 제목",
            content = "테스트 내용",
            isSpoiler = false
        )
    }

    @Test
    @DisplayName("게시글 생성")
    fun createPost() {
        // 유저 Id로 유저를 찾은 뒤 반환 후 저장된 게시글 반환, 테스트 게시글을 생성
        `when`(userAccountRepository.findById(testUser.id!!)).thenReturn(Optional.of(testUser))
        `when`(postRepository.save(any(Post::class.java))).thenReturn(savedPost)
        val testPostDto = postService.create(requestDto)

        /** 검증 **/
        assertEquals(testPostDto.id, expectedPost.id)
        assertEquals(testPostDto.userAccountId, expectedPost.userAccountId)
        assertEquals(testPostDto.nickname, expectedPost.nickname)
        assertEquals(testPostDto.title, expectedPost.title)
        assertEquals(testPostDto.content, expectedPost.content)
        assertEquals(testPostDto.isSpoiler, expectedPost.isSpoiler)

        // PostRepository의 save 메서드가 한 번 호출되었는지 검증
        verify(postRepository).save(any(Post::class.java))
    }

    @Test
    @DisplayName("게시글 Id로 찾기 - 성공")
    fun findPostByIdSuccess() {
        // 저장된 게시글의 id로 특정 게시글을 찾아 반환되도록 설정
        val postId = savedPost.id
        `when`(postRepository.findById(postId!!)).thenReturn(Optional.of(savedPost))

        // 게시글 Id로 조회
        val testPostDto = postService.findById(postId)

        /** 검증 **/
        assertEquals(testPostDto.id, expectedPost.id)
        assertEquals(testPostDto.userAccountId, expectedPost.userAccountId)
        assertEquals(testPostDto.nickname, expectedPost.nickname)
        assertEquals(testPostDto.title, expectedPost.title)
        assertEquals(testPostDto.content, expectedPost.content)
        assertEquals(testPostDto.isSpoiler, expectedPost.isSpoiler)

        // PostRepository의 findById 메서드가 호출되었는지 검증
        verify(postRepository).findById(postId)
    }

    @Test
    @DisplayName("게시글 Id로 찾기 - 실패")
    fun findPostByIdFailure() {
        // 존재하지 않는 게시글 Id로 findById 호출 시 빈 Optional 반환 설정
        val postId = 1000L
        `when`(postRepository.findById(postId)).thenReturn(Optional.empty())

        /** 검증 **/
        // findPostById 메서드를 호출했을 때 EntityNotFoundException이 발생하는지 확인
        assertThrows(ServiceException::class.java) { postService.findById(postId) }

        // PostRepository의 findById 메서드가 호출되었는지 검증
        verify(postRepository).findById(postId)
    }

    @Test
    @DisplayName("게시글 목록 조회 및 검색")
    fun getPostListAndSearch() {
        // 페이지 설정
        val page = 1
        val pageSize = 10
        val pageable: Pageable = PageRequest.of(
            page - 1,
            pageSize,
            Sort.by(
                Sort.Direction.DESC,
                "createdAt"
            )
        )

        // 테스트용 게시글 목록 생성
        val posts = listOf(
            Post(
                id = 1L,
                userAccount = testUser,
                title = "테스트용 게시글1",
                content = "테스트용 게시글 내용1",
                isSpoiler = true,
                createdAt = LocalDateTime.now()
            ),
            Post(
                id = 2L,
                userAccount = testUser,
                title = "테스트용 게시글2",
                content = "테스트용 게시글 내용2",
                isSpoiler = false,
                createdAt = LocalDateTime.now()
            ),
            Post(
                id = 3L,
                userAccount = testUser,
                title = "테스트용 게시글3",
                content = "테스트용 게시글 내용3",
                isSpoiler = true,
                createdAt = LocalDateTime.now().minusDays(1)
            ),
            Post(
                id = 4L,
                userAccount = testUser,
                title = "테스트용 게시글4",
                content = "테스트용 게시글 내용4",
                isSpoiler = false,
                createdAt = LocalDateTime.now()
            ),
            Post(
                id = 5L,
                userAccount = testUser,
                title = "테스트용 게시글5",
                content = "테스트용 게시글 내용5",
                isSpoiler = true,
                createdAt = LocalDateTime.now()
            )
        )

        val postPage = PageImpl(posts, pageable, posts.size.toLong())

        // 전체 게시글 조회
        `when`(postRepository.findAll(pageable)).thenReturn(postPage)
        val testPages = postService.getPostList(page, pageSize, null, null)

        /** 검증 **/
        // 로그 확인
        println(" ========= 게시글 목록 전체 조회 ========= ")
        for (post in testPages.items) {
            println("작성자 닉네임: " + post.nickname)
            println("제목: " + post.title)
            println("내용: " + post.content)
            println("생성 시간: " + post.createdAt)
            println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.isSpoiler)
            println(" =================================== ")
        }
        assertEquals(testPages.items.size, posts.size)
        verify(postRepository).findAll(pageable)

        // 제목으로 검색
        val searchKeywordTitle = "게시글1"
        val titleSearch = posts
            .filter { it.title.contains(searchKeywordTitle) }
        val titleSearchPage = PageImpl(titleSearch, pageable, titleSearch.size.toLong())
        `when`(
            postRepository.findByTitleContaining(
                searchKeywordTitle, pageable
            )
        ).thenReturn(titleSearchPage)
        val titleSearchResult =
            postService.getPostList(page, pageSize, searchKeywordTitle, "title")

        /** 검증 **/
        // 로그 확인
        println(" ========= 게시글 목록 조회(제목) ========= ")
        for (post in titleSearchResult.items) {
            println("작성자 닉네임: " + post.nickname)
            println("제목: " + post.title)
            println("내용: " + post.content)
            println("생성 시간: " + post.createdAt)
            println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.isSpoiler)
            println(" =================================== ")
        }
        assertEquals(titleSearchResult.items.size, titleSearch.size)
        verify(postRepository).findByTitleContaining(
            searchKeywordTitle, pageable
        )

        // 내용으로 검색
        val searchKeywordContent = "내용2"
        val contentSearch = posts
            .filter { it.content.contains(searchKeywordContent) }
        val contentSearchPage =
            PageImpl(contentSearch, pageable, contentSearch.size.toLong())
        `when`(
            postRepository.findByContentContaining(
                searchKeywordContent, pageable
            )
        ).thenReturn(contentSearchPage)
        val contentSearchResult =
            postService.getPostList(page, pageSize, searchKeywordContent, "content")

        /** 검증 **/
        // 로그 확인
        println(" ========= 게시글 목록 조회(내용) ========= ")
        for (post in contentSearchResult.items) {
            println("작성자 닉네임: " + post.nickname)
            println("제목: " + post.title)
            println("내용: " + post.content)
            println("생성 시간: " + post.createdAt)
            println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.isSpoiler)
            println(" =================================== ")
        }
        assertEquals(contentSearchResult.items.size, contentSearch.size)
        verify(postRepository).findByContentContaining(
            searchKeywordContent, pageable
        )

        // 유저 닉네임으로 검색
        val searchKeywordNickname = "유저"
        val nicknameSearch = posts
            .filter { it.userAccount!!.nickname.contains(searchKeywordNickname) }
        val nicknameSearchPage =
            PageImpl(nicknameSearch, pageable, nicknameSearch.size.toLong())
        `when`(
            postRepository.findByUserAccount_NicknameContaining(
                searchKeywordNickname, pageable
            )
        ).thenReturn(nicknameSearchPage)
        val nicknameSearchResult =
            postService.getPostList(page, pageSize, searchKeywordNickname, "nickname")

        /** 검증 **/
        // 로그 확인
        println(" ========= 게시글 목록 조회(유저 닉네임) ========= ")
        for (post in nicknameSearchResult.items) {
            println("작성자 닉네임: " + post.nickname)
            println("제목: " + post.title)
            println("내용: " + post.content)
            println("생성 시간: " + post.createdAt)
            println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.isSpoiler)
            println(" =================================== ")
        }
        assertEquals(nicknameSearchResult.items.size, nicknameSearch.size)
        verify(postRepository).findByUserAccount_NicknameContaining(
            searchKeywordNickname, pageable
        )
    }

    @Test
    @DisplayName("게시글 수정")
    fun updatePost() {
        // 테스트용 수정할 내용 Dto 생성
        val updateRequestDto = PostUpdateRequestDto(
            title = "수정된 제목",
            content = "수정된 내용",
            isSpoiler = false
        )

        // 확인용 엔티티 생성
        val checkPost = Post(
            id = savedPost.id,
            userAccount = testUser,
            title = "수정된 제목",
            content = "수정된 내용",
            isSpoiler = false, // LocalDateTime.now()의 정밀도로 인해 50초 더하기
            createdAt = LocalDateTime.now().plusSeconds(50)
        )

        // 저장된 게시글의 id로 특정 게시글을 찾아 기존 게시글을 반환되도록 설정
        `when`(postRepository.findById(savedPost.id!!)).thenReturn(Optional.of(savedPost))
        `when`(postRepository.save(any(Post::class.java))).thenReturn(checkPost)

        // 수정 메서드 실행 후 변수에 담기
        val testPage = postService.update(savedPost.id!!, updateRequestDto)

        /** 검증 **/
        // 로그
        println(" ========= 게시글 수정 결과 ========= ")
        println("작성자 닉네임: " + testPage.nickname)
        println("제목: " + testPage.title)
        println("내용: " + testPage.content)
        println("생성 시간: " + testPage.createdAt)
        println("스포일러(true면 스포일러, false면 스포일러 아님): " + testPage.isSpoiler)
        println(" =================================== ")

        assertEquals(testPage.title, checkPost.title)
        assertEquals(testPage.content, checkPost.content)
        assertEquals(testPage.isSpoiler, checkPost.isSpoiler)

        // PostRepository의 findById 메서드가 호출되었는지 검증
        verify(postRepository).findById(savedPost.id!!)

        // PostRepository의 save 메서드가 한 번 호출되었는지 검증
        verify(postRepository).save(any(Post::class.java))
    }

    @Test
    @DisplayName("게시글 삭제")
    fun deletePost() {
        // 테스트용 게시글 생성
        val postId = 1000L
        val existingPost = Post(
            id = postId,
            userAccount = null,
            title = null.toString(),
            content = null.toString(),
            isSpoiler = false,
            createdAt = null
        )

        // 저장된 게시글의 id로 특정 게시글을 찾아 기존 게시글을 반환되도록 설정
        `when`(postRepository.findById(postId)).thenReturn(Optional.of(existingPost))

        // 삭제 메서드 실행
        postService.delete(postId)

        /** 검증 **/
        verify(postRepository).delete(existingPost)
    }
}