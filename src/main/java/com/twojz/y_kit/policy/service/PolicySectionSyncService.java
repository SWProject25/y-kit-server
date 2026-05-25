package com.twojz.y_kit.policy.service;

import com.twojz.y_kit.external.policy.dto.YouthPolicy;
import com.twojz.y_kit.policy.domain.dto.PolicyApplicationDto;
import com.twojz.y_kit.policy.domain.dto.PolicyDetailDto;
import com.twojz.y_kit.policy.domain.dto.PolicyQualificationDto;
import com.twojz.y_kit.policy.domain.entity.PolicyApplicationEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyDetailEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyDocumentEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyEntity;
import com.twojz.y_kit.policy.domain.entity.PolicyQualificationEntity;
import com.twojz.y_kit.policy.domain.vo.DocumentParsed;
import com.twojz.y_kit.policy.service.persistence.PolicyApplicationPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyDetailPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyDocumentPersistenceService;
import com.twojz.y_kit.policy.service.persistence.PolicyQualificationPersistenceService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicySectionSyncService {

    private final PolicyDetailPersistenceService policyDetailPersistenceService;
    private final PolicyApplicationPersistenceService policyApplicationPersistenceService;
    private final PolicyQualificationPersistenceService policyQualificationPersistenceService;
    private final PolicyDocumentPersistenceService policyDocumentPersistenceService;
    private final PolicyMapper mapper;

    public boolean syncSections(
            YouthPolicy apiPolicy,
            PolicyEntity policy,
            PolicyDetailEntity detail,
            PolicyApplicationEntity application,
            PolicyQualificationEntity qualification,
            PolicyDocumentEntity document
    ) {
        PolicyDetailDto detailDto = mapper.toDetailRequest(apiPolicy);
        PolicyApplicationDto applicationDto = mapper.toApplicationRequest(apiPolicy);
        PolicyQualificationDto qualificationDto = mapper.toQualificationRequest(apiPolicy);

        boolean detailChanged = updateOrCreateDetail(policy, detail, detailDto);
        boolean applicationChanged = updateOrCreateApplication(policy, application, applicationDto);
        boolean qualificationChanged = updateOrCreateQualification(policy, qualification, qualificationDto);
        boolean documentChanged = updateOrCreateDocument(policy, document, apiPolicy.getSbmsnDcmntCn());

        return detailChanged || applicationChanged || qualificationChanged || documentChanged;
    }

    private boolean updateOrCreateDetail(PolicyEntity policy, PolicyDetailEntity detail, PolicyDetailDto dto) {
        boolean isNew = detail == null;
        if (isNew) {
            detail = PolicyDetailEntity.builder().policy(policy).build();
        }

        detail.updateFromApi(dto);
        if (isNew) {
            policyDetailPersistenceService.save(detail);
        }
        return isNew;
    }

    private boolean updateOrCreateApplication(
            PolicyEntity policy,
            PolicyApplicationEntity application,
            PolicyApplicationDto dto
    ) {
        boolean isNew = application == null;
        if (isNew) {
            application = PolicyApplicationEntity.builder().policy(policy).build();
        }

        application.updateFromApi(dto);
        if (isNew) {
            policyApplicationPersistenceService.save(application);
        }
        return isNew;
    }

    private boolean updateOrCreateQualification(
            PolicyEntity policy,
            PolicyQualificationEntity qualification,
            PolicyQualificationDto dto
    ) {
        boolean isNew = qualification == null;
        if (isNew) {
            qualification = PolicyQualificationEntity.builder().policy(policy).build();
        }

        qualification.updateFromApi(dto);
        if (isNew) {
            policyQualificationPersistenceService.save(qualification);
        }
        return isNew;
    }

    private boolean updateOrCreateDocument(PolicyEntity policy, PolicyDocumentEntity document, String original) {
        if (isEmptyDocument(original)) {
            return false;
        }

        boolean isNew = document == null;
        if (isNew) {
            document = PolicyDocumentEntity.builder().policy(policy).build();
        }

        if (isNew || !Objects.equals(document.getDocumentsOriginal(), original)) {
            document.updateOriginal(original);
            DocumentParsed parsed = DocumentPreprocessor.parse(original);
            document.updateParsed(parsed);
            policyDocumentPersistenceService.save(document);
            return true;
        }

        return false;
    }

    private boolean isEmptyDocument(String original) {
        return original == null ||
                original.trim().isEmpty() ||
                original.equals("-") ||
                original.equals("없음");
    }
}
