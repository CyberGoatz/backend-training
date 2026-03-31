package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.dto.assessmentlevel.preview.ExtendedMatchingStatementPreviewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.preview.QuestionChoicePreviewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.preview.QuestionPreviewDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.ExtendedMatchingOptionDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.ExtendedMatchingStatementDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionChoiceDTO;
import cz.cyberrange.platform.training.api.dto.assessmentlevel.question.QuestionDTO;
import cz.cyberrange.platform.training.api.enums.QuestionType;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingOption;
import cz.cyberrange.platform.training.persistence.model.question.ExtendedMatchingStatement;
import cz.cyberrange.platform.training.persistence.model.question.Question;
import cz.cyberrange.platform.training.persistence.model.question.QuestionChoice;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * The InfoLevelMapper is an utility class to map items into data transfer objects. It provides the
 * implementation of mappings between Java bean type InfoLevelMapper and DTOs classes. Code is
 * generated during compile time.
 */
@Mapper(
    componentModel = "spring",
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionMapper extends ParentMapper {
  Question mapToEntity(QuestionDTO dto);

  @AfterMapping
  default void setCorrectOption(@MappingTarget Question target, @Context QuestionDTO questionDTO) {
    questionDTO
        .getExtendedMatchingStatements()
        .forEach(
            statementDTO -> {
              ExtendedMatchingOption correctOption =
                  target.getExtendedMatchingOptions().get(statementDTO.getCorrectOptionOrder());
              target
                  .getExtendedMatchingStatements()
                  .get(statementDTO.getOrder())
                  .setExtendedMatchingOption(correctOption);
            });
  }

  QuestionChoice mapToEntity(QuestionChoiceDTO dto);

  ExtendedMatchingStatement mapToEntity(ExtendedMatchingStatementDTO dto);

  ExtendedMatchingOption mapToEntity(ExtendedMatchingOptionDTO dto);

  @Named("questionWithoutId")
  @Mapping(target = "id", ignore = true)
  @Mapping(source = "choices", target = "choices", qualifiedByName = "questionChoiceWithoutId")
  @Mapping(
      source = "extendedMatchingStatements",
      target = "extendedMatchingStatements",
      qualifiedByName = "extendedMatchingStatementWithoutId")
  @Mapping(
      source = "extendedMatchingOptions",
      target = "extendedMatchingOptions",
      qualifiedByName = "extendedMatchingOptionWithoutId")
  QuestionDTO mapToQuestionDTOWithoutId(Question entity);

  QuestionDTO mapToQuestionDTO(Question entity);

  QuestionPreviewDTO mapToQuestionPreviewDTO(Question entity);

  @Named("questionChoiceWithoutId")
  @Mapping(target = "id", ignore = true)
  QuestionChoiceDTO mapToQuestionChoiceDTOWithoutId(QuestionChoice entity);

  QuestionChoiceDTO mapToQuestionChoiceDTO(QuestionChoice entity);

  QuestionChoicePreviewDTO mapToQuestionChoicePreviewDTO(QuestionChoice entity);

  @Named("extendedMatchingStatementWithoutId")
  @Mapping(target = "id", ignore = true)
  @Mapping(source = "extendedMatchingOption", target = "correctOptionOrder")
  ExtendedMatchingStatementDTO mapToExtendedMatchingStatementDTOWithoutId(
      ExtendedMatchingStatement entity);

  @Mapping(source = "extendedMatchingOption", target = "correctOptionOrder")
  ExtendedMatchingStatementDTO mapToExtendedMatchingStatementDTO(ExtendedMatchingStatement entity);

  ExtendedMatchingStatementPreviewDTO mapToExtendedMatchingStatementPreviewDTO(
      ExtendedMatchingStatement entity);

  @Named("extendedMatchingOptionWithoutId")
  @Mapping(target = "id", ignore = true)
  ExtendedMatchingOptionDTO mapToExtendedMatchingOptionDTOWithoutId(ExtendedMatchingOption entity);

  ExtendedMatchingOptionDTO mapToExtendedMatchingOptionDTO(ExtendedMatchingOption entity);

  default Integer mapToOptionOrder(ExtendedMatchingOption entity) {
    if (entity == null) {
      return null;
    } else {
      return entity.getOrder();
    }
  }

  QuestionType mapToApiType(
      cz.cyberrange.platform.training.persistence.model.enums.QuestionType questionType);
}
