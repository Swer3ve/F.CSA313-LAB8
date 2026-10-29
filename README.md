# F.CSA313 — Лаб 8: Mockito

Г.Эрхэмбаяр B232270055

## Орчин

```
java version "27" 2026-09-15
Java(TM) SE Runtime Environment (build 27+35-2325)
Java HotSpot(TM) 64-Bit Server VM (build 27+35-2325, mixed mode, sharing)

Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)
Java version: 27, vendor: Oracle Corporation
OS name: "windows 11", version: "10.0", arch: "amd64"
```

## Хэсэг A — тестүүд

`CourseRegistrationServiceTest` дотор A1–A8 бичигдсэн. Нийт тест: **10** (7 энгийн + A8-ийн 3 мөр), `results/mvn-test.txt`-ийн сүүлийн `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`.

**A8-ийн `lenient()`:** Мөр бүрд өөр нөхцөл тул `courseExists`, `prerequisites` зэрэг stub зарим мөрөнд ашиглагдахгүй (жишээ нь оюутан олдохгүй мөрөнд). MockitoExtension-ий strict stubs ашиглагдаагүй stub-ыг `UnnecessaryStubbingException`-оор унагаадаг тул эдгээр stub-ыг `lenient()`-ээр зарласан.

## Хэсэг B — мини мутаци

| # | Мутаци | Унасан тест(үүд) | Файл |
|---|--------|------------------|------|
| 1 | `if (!student.active()) ...` комментлосон | A3 `register_inactiveStudent_returnsInactiveAndNeverTouchesEmail` (Failure: `ERROR_NO_COURSE` буцсан); A8 `register_errorCases_returnExpectedCode[2]` (Failure: `OK` буцсан) | `results/mvn-test-mutant-1.txt` |
| 2 | `saveRegistration(...)` дуудалтыг комментлосон | A1 `register_activeStudentWithPrereqs_returnsOkSavesAndSendsEmail`; A5 `register_emailSenderThrows_stillReturnsOkAndSaves` (хоёул `Wanted but not invoked: saveRegistration`) | `results/mvn-test-mutant-2.txt` |
| 3 | `\|\| courseId == null` устгасан | A6 `register_nullArgument_throwsIllegalArgumentAndTouchesNothing` (`register("S1", null)`-д exception шидэгдээгүй) | `results/mvn-test-mutant-3.txt` |

Мутант бүр `BUILD FAILURE` өгсөн, `UnnecessaryStubbingException` гараагүй.

**Дүгнэлт:** Гурван мутантыг бүгдийг тест барьсан. Мутант 3 нь A6 нь эхнээс нь `studentId`, `courseId` хоёр аргументын `null`-ийг тус тусад нь шалгадаг тул шууд баригдсан, тиймээс A6-г нэмж засах шаардлагагүй болсон. Мутант 1-д A3 нь `ERROR_NO_COURSE` буцсанаар унасан нь тестийн stub-ууд кодын урсгалыг зөв тусгаж байгааг харуулна. Мутант 2-ыг `verify(...saveRegistration...)` барьсан бөгөөд өгөөгдлийг шууд шалгах боломжгүй үед verify нь үр дүнг батлах гол хэрэгсэл болохыг үзүүлсэн.

## Дүгнэлт (mock, fake, verify)

Mock нь Mockito-оор ажиллах үед нь тухайн хамаарлын дуудалтыг бүртгэж, хариуг нь тест бүрд stub-аар зааж өгдөг бол гараар бичсэн fake нь жинхэнэ логиктой (жишээ нь санах ойд өгөгдөл хадгалдаг) жижиг хэрэгжүүлэлт юм. Энэ лабын `StudentRepository`-г fake болгож бичвэл тест бүрт өгөгдөл бэлдэж, fake-ийн алдааг тусад нь шалгах шаардлагатай болох байсан, харин mock-оор тест бүр өөрт хэрэгтэй хариуг ганц мөрөөр зааж өгөхөд хангалттай байлаа. Харин тест олон дуудалтын хооронд төлөв хадгалах шаардлагатай үед (хадгалсан өгөгдлийг дараа нь буцааж унших гэх мэт) fake илүү тохиромжтой. Тестлэж буй классаа өөрийг нь, мөн `Student` шиг энгийн өгөгдлийн record-ыг mock хийх ёсгүй, учир нь тэгвэл бодит логик биш mock-ын тохиргоог л шалгах болно. Mock хийх зүйл нь өгөгдлийн сан, и-мэйл илгээгч шиг удаан, гадаад, тестийг тогтворгүй болгодог хамаарал байх ёстой. Verify хэтрүүлбэл, жишээ нь бүх дуудалтын дарааллыг шалгавал тест нь үр дүнг биш хэрэгжүүлэлтийн дэлгэрэнгүйг шалгаж эхэлдэг тул кодыг логикийг нь өөрчлөхгүйгээр refactor хийхэд л тест унаж, тест засах ажил нэмэгдэнэ. Хэсэг B-д `saveRegistration` дуудалтыг устгахад verify-тай тестүүд барьсан нь verify-ийн хэрэгтэй тал, харин `never()`-ийг хэт олон газар хэрэглэх нь эрсдэл болохыг харуулсан. Иймээс үр дүн (буцаасан утга, гарах нөлөө) чухал үед verify хэрэглэж, зөвхөн дотоод алхмуудыг шалгахдаа зайлсхийх нь зөв.