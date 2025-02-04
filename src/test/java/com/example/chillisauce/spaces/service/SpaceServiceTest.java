package com.example.chillisauce.spaces.service;

import com.example.chillisauce.security.UserDetailsImpl;
import com.example.chillisauce.spaces.dto.request.SpaceRequestDto;
import com.example.chillisauce.spaces.dto.response.SpaceListResponseDto;
import com.example.chillisauce.spaces.dto.response.SpaceResponseDto;
import com.example.chillisauce.spaces.entity.Floor;
import com.example.chillisauce.spaces.entity.Space;
import com.example.chillisauce.spaces.exception.SpaceErrorCode;
import com.example.chillisauce.spaces.exception.SpaceException;
import com.example.chillisauce.spaces.repository.FloorRepository;
import com.example.chillisauce.spaces.repository.MrRepository;
import com.example.chillisauce.spaces.repository.SpaceRepository;
import com.example.chillisauce.users.entity.Companies;
import com.example.chillisauce.users.repository.CompanyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.chillisauce.fixture.FixtureFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class SpaceServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private SpaceRepository spaceRepository;
    @Mock
    private FloorRepository floorRepository;
    @InjectMocks
    private SpaceService spaceService;


    @Nested
    @DisplayName("성공케이스")
    class SuccessCase {

        Companies companies = Company_생성();
        Floor floor = Floor_생성_아이디_지정(1L);
        UserDetailsImpl details = details_권한_ADMIN_유저_네임_NULL(companies);
        Space space = Space_생성_아이디_지정_회사_지정(1L, companies);
        @Test
        void Floor_안에_Space_생성() {
            //given

            SpaceRequestDto spaceRequestDto = new SpaceRequestDto();
            when(spaceRepository.save(any(Space.class))).thenReturn(space);
            when(floorRepository.findById(floor.getId())).thenReturn(Optional.of(floor));
            when(companyRepository.findByCompanyName(companies.getCompanyName())).thenReturn(Optional.of(Companies.builder().build()));

            //when
            SpaceResponseDto spaceResponseDto = spaceService.createSpaceInFloor(companies.getCompanyName(), spaceRequestDto, details, 1L);

            //then
            assertNotNull(spaceResponseDto);
            assertEquals(space.getSpaceName(), spaceResponseDto.getSpaceName());
        }


        @Test
        void Space_생성() {
            //given
            SpaceRequestDto spaceRequestDto = new SpaceRequestDto();
            when(spaceRepository.save(any(Space.class))).thenReturn(space);
            when(companyRepository.findByCompanyName(companies.getCompanyName())).thenReturn(Optional.of(companies));
            //when
            SpaceResponseDto spaceResponseDto = spaceService.createSpace(companies.getCompanyName(), spaceRequestDto, details);

            //then
            assertNotNull(spaceResponseDto);
            assertEquals(space.getSpaceName(), spaceResponseDto.getSpaceName());
        }


        @Test
        void Space_공간_전체_조회() {
            List<Space> spaceList = Collections.singletonList(space);
            List<SpaceListResponseDto> responseDto = spaceList.stream().map(SpaceListResponseDto::new).toList();
            when(spaceRepository.getSpaceAllList(companies.getCompanyName())).thenReturn(spaceList.stream().map(SpaceListResponseDto::new).collect(Collectors.toList()));

            // when
            List<SpaceListResponseDto> result = spaceService.allSpacelist(companies.getCompanyName(), details);

            // then
            assertNotNull(result);
            assertEquals(responseDto.size(), result.size());
            assertThat(result).allSatisfy(responseSpace -> {
                assertThat(responseSpace.getSpaceName()).isEqualTo("testSpace");
            });
        }

        @Test
        void Space_공간_선택_조회() {
            //given
            List<Space> spaceList = Collections.singletonList(space);
            when(spaceRepository.getSpacesList(space.getId())).thenReturn(spaceList.stream().map(SpaceResponseDto::new).collect(Collectors.toList()));
            when(spaceRepository.findById(eq(space.getId()))).thenReturn(Optional.of(space));
            //when
            List<SpaceResponseDto> result = spaceService.getSpacelist(companies.getCompanyName(), space.getId(), details);

            //Then
            assertNotNull(result);
            SpaceResponseDto spaceResponseDto = SpaceResponseDto.builder().space(space).floorName(floor.getFloorName()).floorId(floor.getId()).build();
            assertEquals("testSpace", spaceResponseDto.getSpaceName());
            assertEquals(floor.getId(), spaceResponseDto.getFloorId());
            assertEquals(floor.getFloorName(), spaceResponseDto.getFloorName());
        }

        @Test
        void Space_공간_선택_조회_Floor_null() {
            //given
            List<Space> spaceList = Collections.singletonList(space);
            when(spaceRepository.getSpacesList(space.getId())).thenReturn(spaceList.stream().map(SpaceResponseDto::new).collect(Collectors.toList()));
            when(spaceRepository.findById(space.getId())).thenReturn(Optional.of(space));
            //when
            List<SpaceResponseDto> result = spaceService.getSpacelist(companies.getCompanyName(), space.getId(), details);

            //Then
            assertNotNull(result);
            SpaceResponseDto spaceResponseDto = SpaceResponseDto.builder().space(space).floorName(floor.getFloorName()).floorId(floor.getId()).build();
            assertEquals("testSpace", spaceResponseDto.getSpaceName());
            assertEquals(floor.getId(), spaceResponseDto.getFloorId());
            assertEquals(floor.getFloorName(), spaceResponseDto.getFloorName());
        }


        @Test
        void Space_공간_수정() {
            //given
            when(companyRepository.findByCompanyName(companies.getCompanyName())).thenReturn(Optional.of(companies));
            when(spaceRepository.findByIdAndCompanies(space.getId(), companies)).thenReturn(Optional.of(space));
            SpaceRequestDto spaceRequestDto = new SpaceRequestDto("수정 입니까?");
            //when
            SpaceResponseDto spaceResponseDto = spaceService.updateSpace(companies.getCompanyName(), space.getId(), spaceRequestDto, details);

            //Then
            assertNotNull(spaceResponseDto);
            assertEquals("수정 입니까?", spaceResponseDto.getSpaceName());
        }

        @Test
        void Space_공간_삭제() {
            //given
            when(companyRepository.findByCompanyName(companies.getCompanyName())).thenReturn(Optional.of(companies));
            when(spaceRepository.findByIdAndCompanies(space.getId(), companies)).thenReturn(Optional.of(space));
            doNothing().when(spaceRepository).clearAllReservationsForSpace(space.getId());
            doNothing().when(spaceRepository).deleteById(space.getId());

            //when
            SpaceResponseDto spaceResponseDto = spaceService.deleteSpace(companies.getCompanyName(), space.getId(), details);

            //Then
            assertNotNull(spaceResponseDto);
            assertEquals("testSpace", spaceResponseDto.getSpaceName());
        }

        @Nested
        @DisplayName("공간 권한 없음 예외 케이스")
        class NotPermissionExceptionCase {
            // given
            UserDetailsImpl details = details_권한_USER(companies);
            SpaceRequestDto requestDto = new SpaceRequestDto("testSpace");

            public static void NOT_HAVE_PERMISSION_EXCEPTION(SpaceErrorCode expectedErrorCode, Executable executable) {
                SpaceException exception = assertThrows(SpaceException.class, executable);
                assertEquals(expectedErrorCode, exception.getErrorCode());
            }

            @Test
            void Floor_안에_Space_생성_권한_없음() {
                //When,Then
                NotPermissionExceptionCase.NOT_HAVE_PERMISSION_EXCEPTION(SpaceErrorCode.NOT_HAVE_PERMISSION, () -> {
                    spaceService.createSpaceInFloor(companies.getCompanyName(), requestDto, details, floor.getId());
                });
            }

            @Test
            void 공간_생성_권한_없음() {
                //When,Then
                NotPermissionExceptionCase.NOT_HAVE_PERMISSION_EXCEPTION(SpaceErrorCode.NOT_HAVE_PERMISSION, () -> {
                    spaceService.createSpace(companies.getCompanyName(), requestDto, details);
                });
            }

            @Test
            void 공간_수정_권한_없음() {
                //given
                NotPermissionExceptionCase.NOT_HAVE_PERMISSION_EXCEPTION(SpaceErrorCode.NOT_HAVE_PERMISSION, () -> {
                    spaceService.updateSpace(companies.getCompanyName(), space.getId(), requestDto, details);
                });
            }

            @Test
            void 공간_삭제_권한_없음() {
                //given
                NotPermissionExceptionCase.NOT_HAVE_PERMISSION_EXCEPTION(SpaceErrorCode.NOT_HAVE_PERMISSION, () -> {
                    spaceService.deleteSpace(companies.getCompanyName(), space.getId(), details);
                });
            }
        }

        @Nested
        @DisplayName("해당 회사 권한 없음 예외 케이스")
        class CompanyNotPermissionExceptionCase {
            // given
            Companies differentCompanyName = Different_Company_생성();

            public static void COMPANIES_NOT_PERMISSION_EXCEPTION(SpaceErrorCode expectedErrorCode, Executable executable) {
                SpaceException exception = assertThrows(SpaceException.class, executable);
                assertEquals(expectedErrorCode, exception.getErrorCode());
            }

            @Test
            void 전체_공간_조회_해당_회사_권한_없음() {
                // when & then
                CompanyNotPermissionExceptionCase.COMPANIES_NOT_PERMISSION_EXCEPTION(SpaceErrorCode.NOT_HAVE_PERMISSION_COMPANIES, () -> {
                    spaceService.allSpacelist(differentCompanyName.getCompanyName(), details);
                });
            }

            @Test
            void 선택_공간_조회_해당_회사_권한_없음() {
                // when & then
                CompanyNotPermissionExceptionCase.COMPANIES_NOT_PERMISSION_EXCEPTION(SpaceErrorCode.NOT_HAVE_PERMISSION_COMPANIES, () -> {
                    spaceService.getSpacelist(differentCompanyName.getCompanyName(), space.getId(), details);
                });
            }
        }

        @Nested
        @DisplayName("회사 이름 으로 찾을 수 없는 경우")
        class findByCompanyName {
            // given
            SpaceRequestDto requestDto = new SpaceRequestDto("testSpace");

            public static void COMPANIES_NOT_FOUND_EXCEPTION(SpaceErrorCode expectedErrorCode, Executable executable) {
                SpaceException exception = assertThrows(SpaceException.class, executable);
                assertEquals(expectedErrorCode, exception.getErrorCode());
            }

            @Test
            void Floor_안에_Space_생성_해당_회사_없음() {
                //when
                when(floorRepository.findById(floor.getId())).thenReturn(Optional.of(floor));
                //then
                findByCompanyName.COMPANIES_NOT_FOUND_EXCEPTION(SpaceErrorCode.COMPANIES_NOT_FOUND, () -> {
                    spaceService.createSpaceInFloor(companies.getCompanyName(), requestDto, details, floor.getId());
                });
            }

            @Test
            void Space_생성_해당_회사_없음() {
                //when,then
                findByCompanyName.COMPANIES_NOT_FOUND_EXCEPTION(SpaceErrorCode.COMPANIES_NOT_FOUND, () -> {
                    spaceService.createSpace(companies.getCompanyName(), requestDto, details);
                });
            }
        }


        @Nested
        @DisplayName("회사 이름 으로 찾을 수 없는 경우")
        class findByFloorId {
            @Test
            void Floor_안에_공간_생성_해당_Floor_없음() {
                //given
                when(floorRepository.findById(floor.getId())).thenReturn(Optional.empty());
                SpaceRequestDto spaceRequestDto = new SpaceRequestDto("test 생성");
                //when,then
                SpaceException exception = assertThrows(SpaceException.class, () -> {
                    spaceService.createSpaceInFloor(companies.getCompanyName(), spaceRequestDto, details, floor.getId());
                });
                assertEquals(SpaceErrorCode.FLOOR_NOT_FOUND, exception.getErrorCode());
            }
        }

        @Nested
        @DisplayName("회사 이름과 공간 ID로 층을 찾을 수 없는 경우")
        class findCompanyNameAndFloorId {
            @Test
            void 해당_회사_없음() {
                //given
                when(companyRepository.findByCompanyName(companies.getCompanyName())).thenReturn(Optional.empty());

                //When,Then
                SpaceException exception = assertThrows(SpaceException.class, () -> {
                    spaceService.findCompanyNameAndSpaceId(companies.getCompanyName(), space.getId());
                });
                assertEquals(SpaceErrorCode.COMPANIES_NOT_FOUND, exception.getErrorCode());
            }

            @Test
            void 해당_회사_아이디_없음() {
                //given
                when(companyRepository.findByCompanyName(companies.getCompanyName())).thenReturn(Optional.of(companies));
                when(spaceRepository.findByIdAndCompanies(space.getId(), companies)).thenReturn(Optional.empty());

                //when,Then
                SpaceException exception = assertThrows(SpaceException.class, () -> {
                    spaceService.findCompanyNameAndSpaceId(companies.getCompanyName(), space.getId());
                });
                assertEquals(SpaceErrorCode.SPACE_NOT_FOUND, exception.getErrorCode());
            }
        }

        @Nested
        @DisplayName("유저 회사와 생성하려는 회사가 일치하지 않을때")
        class CompaniesPermissionException {
            //given
            Companies companies = Different_Company_생성();
            SpaceRequestDto requestDto = new SpaceRequestDto("testSpace");

            public static void COMPANIES_PERMISSION_EXCEPTION(SpaceErrorCode expectedErrorCode, Executable executable) {
                SpaceException exception = assertThrows(SpaceException.class, executable);
                assertEquals(expectedErrorCode, exception.getErrorCode());
            }

            //when, then
            @Test
            void 플로우_안에_공간_생성_권한_없음() {
                // when & then
                when(floorRepository.findById(floor.getId())).thenReturn(Optional.ofNullable(floor));
                when(companyRepository.findByCompanyName(companies.getCompanyName())).thenReturn(Optional.ofNullable(companies));
                CompaniesPermissionException.COMPANIES_PERMISSION_EXCEPTION(SpaceErrorCode.NOT_HAVE_PERMISSION_COMPANIES, () -> {
                    spaceService.createSpaceInFloor(companies.getCompanyName(), requestDto, details, floor.getId());
                });
            }

            @Test
            void 공간_생성_권한_없음() {
                // when & then
                when(companyRepository.findByCompanyName(companies.getCompanyName())).thenReturn(Optional.ofNullable(companies));
                CompaniesPermissionException.COMPANIES_PERMISSION_EXCEPTION(SpaceErrorCode.NOT_HAVE_PERMISSION_COMPANIES, () -> {
                    spaceService.createSpace(companies.getCompanyName(), requestDto, details);
                });

            }
        }
    }
}





