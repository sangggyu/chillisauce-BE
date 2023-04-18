package com.example.chillisauce.users.controller;

import com.example.chillisauce.jwt.JwtUtil;
import com.example.chillisauce.users.dto.*;
import com.example.chillisauce.users.exception.UserErrorCode;
import com.example.chillisauce.users.exception.UserException;
import com.example.chillisauce.users.exception.UserExceptionHandler;
import com.example.chillisauce.users.service.EmailServiceImpl;
import com.example.chillisauce.users.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.util.HashMap;

import static com.example.chillisauce.docs.ApiDocumentUtil.getDocumentRequest;
import static com.example.chillisauce.docs.ApiDocumentUtil.getDocumentResponse;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
class UserControllerTest {

    @InjectMocks
    private UserController userController;
    @Mock
    private UserService userService;
    @Mock
    private EmailServiceImpl emailService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void init(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new UserExceptionHandler())
                .apply(documentationConfiguration(restDocumentation))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("유저 컨트롤러 성공 케이스")
    class ControllerSuccessCase {

        @DisplayName("관리자 회원가입")
        @Test
        void success1() throws Exception {
            //given
            SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                    .email("123@123")
                    .password("1234qwer!")
                    .passwordCheck("1234qwer!")
                    .username("루피")
                    .companyName("뽀로로랜드")
                    .certification("1234")
                    .build();
            AdminSignupResponseDto responseDto = AdminSignupResponseDto.builder()
                    .certification("1234")
                    .build();

            when(userService.signupAdmin(any(), any())).thenReturn(responseDto);

            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                    .post("/users/signup/admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));

            //then
            result.andExpect(status().isOk())
                    .andDo(document("post-signupAdmin",
                            getDocumentRequest(),
                            getDocumentResponse(),
                            responseFields(
                                    fieldWithPath("statusCode").type(JsonFieldType.NUMBER).description("상태코드"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("결과메시지"),
                                    fieldWithPath("data").type(JsonFieldType.OBJECT).description("결과값"),
                                    fieldWithPath("data.certification").type(JsonFieldType.STRING).description("인증번호")
                            )
                    ));
        }

        @DisplayName("사원 회원가입")
        @Test
        void success2() throws Exception {
            //given
            String answer = "일반 회원 가입 성공";
            UserSignupRequestDto requestDto = UserSignupRequestDto.builder()
                    .email("123@123")
                    .password("1234qwer!")
                    .passwordCheck("1234qwer!")
                    .username("루피")
                    .certification("1234")
                    .build();
//            when(userService.signupUser(requestDto)).thenReturn(answer);    //Strict stubbing argument mismatch. Please check: 에러 발생 구문
            when(userService.signupUser(any())).thenReturn(answer);    // 수정
            //에러가 발생하는 이유 : 사진 참조

            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                    .post("/users/signup/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)));

            //then
            result.andExpect(status().isOk());
        }

        @DisplayName("이메일 전송")
        @Test
        void success3() throws Exception {
            //given
            String certificationKey = "1234";
            SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                    .email("123@123")
                    .password("1234qwer!")
                    .passwordCheck("1234qwer!")
                    .username("루피")
                    .companyName("뽀로로랜드")
                    .certification("1234")
                    .build();
            when(emailService.sendSimpleMessage(any())).thenReturn("certification : " + certificationKey);

            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                    .post("/users/signup/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));

            //then
            result.andExpect(status().isOk());
        }

        @DisplayName("로그인")
        @Test
        void success4() throws Exception {
            //given
            LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                    .email("123@123")
                    .password("1234qwer!")
                    .build();
            when(userService.Login(any(), any())).thenReturn("로그인 성공");

            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                    .post("/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequestDto)));

            //then
            result.andExpect(status().isOk());
        }

        @DisplayName("인증번호 확인")
        @Test
        void success5() throws Exception {
            //given
            HashMap<String, String> certification = new HashMap<>();
            certification.put("certification", "1234");

            when(userService.checkCertification(certification.get("certification"))).thenReturn("인증번호가 확인 되었습니다.");

            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                    .post("/users/signup/match")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(certification)));

            //then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("인증번호가 확인 되었습니다."));
        }

        @DisplayName("엑세스 토큰 재발급")
        @Test
        void success6() throws Exception{
            //given
            String refreshToken = "fakeToken";

            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                    .get("/users/refresh")
                    .header(JwtUtil.AUTHORIZATION_HEADER, refreshToken));

            result.andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("유저 컨트롤러 실패 케이스")
    class ControllerFailCase {


        @DisplayName("관리자 회원가입 실패 (잘못된 요청_이메일 형식 오류)")
        @Test
        void fail1() throws Exception {
            //given
            SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                    .email("뽀로로")
                    .password("1234qwer!")
                    .passwordCheck("1234qwer!")
                    .username("루피")
                    .companyName("뽀로로랜드")
                    .certification("1234")
                    .build();

            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                    .post("/users/signup/admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));

            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."));

        }

        @DisplayName("관리자 회원가입 실패 (잘못된 요청_비밀번호 형식 오류)")
        @Test
        void fail2() throws Exception {
            //given
            SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                    .email("123@123")
                    .password("1234")
                    .passwordCheck("1234")
                    .username("루피")
                    .companyName("뽀로로랜드")
                    .certification("1234")
                    .build();

            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                    .post("/users/signup/admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));

            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("비밀번호는 8 ~ 16자리 영문, 숫자, 특수문자를 조합하여 입력하세요."));

        }

        @DisplayName("사원 회원가입 실패 (잘못된 요청_이메일 형식 오류)")
        @Test
        void fail3() throws Exception {
            //given
            UserSignupRequestDto signupRequestDto = UserSignupRequestDto.builder()
                    .email("뽀로로")
                    .password("1234qwer!")
                    .passwordCheck("1234qwer!")
                    .username("루피")
                    .certification("1234")
                    .build();

            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                    .post("/users/signup/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));

            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."));

        }

        @DisplayName("사원 회원가입 실패 (잘못된 요청_비밀번호 형식 오류)")
        @Test
        void fail4() throws Exception {
            //given
            UserSignupRequestDto signupRequestDto = UserSignupRequestDto.builder()
                    .email("뽀로로@뽀로로랜드")
                    .password("1234")
                    .passwordCheck("1234")
                    .username("루피")
                    .certification("1234")
                    .build();

            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                    .post("/users/signup/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequestDto)));

            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("비밀번호는 8 ~ 16자리 영문, 숫자, 특수문자를 조합하여 입력하세요."));

        }

//        @DisplayName("로그인 실패 (잘못된 요청_이메일 형식 오류)")
//        @Test
//        void fail5() throws Exception {
//            //given
//            LoginRequestDto signupRequestDto = LoginRequestDto.builder()
//                    .email("뽀로로")
//                    .password("1234qwer!")
//                    .build();
//
//            //when
//            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
//                    .post("/users/login")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .accept(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(signupRequestDto)));
//
//            result.andExpect(status().isBadRequest())
//                    .andExpect(jsonPath("$.message").value("이메일 형식이 올바르지 않습니다."));
//
//        }
//
//        @DisplayName("로그인 실패 (잘못된 요청_비밀번호 형식 오류)")
//        @Test
//        void fail6() throws Exception {
//            //given
//            LoginRequestDto signupRequestDto = LoginRequestDto.builder()
//                    .email("뽀로로@뽀로로랜드")
//                    .password("1234")
//                    .build();
//
//            //when
//            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
//                    .post("/users/login")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .accept(MediaType.APPLICATION_JSON)
//                    .content(objectMapper.writeValueAsString(signupRequestDto)));
//
//            result.andExpect(status().isBadRequest())
//                    .andExpect(jsonPath("$.message").value("비밀번호는 8 ~ 16자리 영문, 숫자, 특수문자를 조합하여 입력하세요."));
//
//        }

        @DisplayName("인증번호 확인 실패 (유효하지 않은 인증번호)")
        @Test
        void failTest() throws Exception {
            //given
            HashMap<String, String> certification = new HashMap<>();
            certification.put("certification", "1234");

            when(userService.checkCertification(certification.get("certification"))).thenThrow(new UserException(UserErrorCode.INVALID_CERTIFICATION));

            //when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                    .post("/users/signup/match")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(certification)));

            //then
            result.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.statusCode").value(UserErrorCode.INVALID_CERTIFICATION.getHttpStatus().value()))
                    .andExpect(jsonPath("$.message").value("인증번호가 유효하지 않습니다"));

        }

    }
}