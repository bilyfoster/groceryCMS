export type IntakeQuestionType = "single" | "multi";

export type IntakeCriterion = "FOCUS_AREA" | "MODALITY" | "DEMOGRAPHIC" | "SERVICE_DELIVERY";

export interface IntakeOption {
  value: string;
  label: string;
}

export interface IntakeQuestion {
  id: string;
  label: string;
  type: IntakeQuestionType;
  criterion: IntakeCriterion;
  required: boolean;
  options: IntakeOption[];
}

export interface IntakeQuestionnaire {
  title: string;
  description: string;
  questions: IntakeQuestion[];
}

export interface IntakeAnswers {
  [questionId: string]: string[];
}

export interface IntakeMatchRequest {
  answers: IntakeAnswers;
}
