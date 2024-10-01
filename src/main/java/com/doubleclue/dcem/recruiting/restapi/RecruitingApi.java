package com.doubleclue.dcem.recruiting.restapi;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import com.doubleclue.dcem.core.entities.BranchLocation;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.jpa.ApiFilterItem.OperatorEnum;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.recruiting.entities.CandidateEntity;
import com.doubleclue.dcem.recruiting.entities.CareerLevelEntity;
import com.doubleclue.dcem.recruiting.entities.ClassificationEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity;
import com.doubleclue.dcem.recruiting.entities.VacancyEntity_;
import com.doubleclue.dcem.recruiting.entities.VacancyStatusEntity;
import com.doubleclue.dcem.recruiting.logic.RecruitingModule;
import com.doubleclue.dcem.recruiting.restapi.impl.ApiRecruitingImpl;
import com.doubleclue.dcem.recruiting.restapi.model.ApiRecruitingFile;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Path("/" + RecruitingModule.MODULE_ID)
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@OpenAPIDefinition(info = @Info(title = "DoubleClue Recruiting API", version = "3.2.0", description = "This a REST-API interface for DoubleClue Recruiting model. Use OpenAPI tools  to generate sources for your client sources."
		+ "Example for a JSON ApiFilterItem: [{\"name\":\"title\",\"value\":\"blabla\",\"operator\":\"LIKE\"}]", termsOfService = "", contact = @Contact(email = "support@doubleclue.com")))

public class RecruitingApi {
	

	/**
	 * 
	 * @param text
	 * @param securityContext
	 * @return the text given 
	 * @throws DcemApiException
	 */
	@GET
	@Path("/echo")
	@Consumes({ MediaType.TEXT_PLAIN })
	@Produces({ MediaType.TEXT_PLAIN })
	@Operation(description = "return the text received ")
	@ApiResponse (description = "This is for testing purposes")
	public Response echo(@QueryParam("text") String text, @Context SecurityContext securityContext)  {
		return Response.ok().entity(text).build();
	}

	@POST
	@Path("/getVacancies")
	@ApiResponse (description = "Retrieves Vacancies, optionally using the ApiFilter in the body",  content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VacancyEntity.class))) }) 
	public Response getVacancies(List<ApiFilterItem> filterItems,
			@Parameter(name="language", description = "If langauge is not set, then all languages are returned") @QueryParam("language") SupportedLanguage language,
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("100") @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		if (filterItems == null) {
			filterItems = new ArrayList<ApiFilterItem>(1);
		}
		filterItems.add(new ApiFilterItem(VacancyEntity_.ARCHIVED.toString(), Boolean.FALSE.toString(), OperatorEnum.EQUALS));
		return apiServiceImpl.getVacancies(language, filterItems, offset, maxResults);
	}
	
	@GET
	@Path("/getVacancies")
	@ApiResponse (description = "Retrieves Vacancies, optionally using the ApiFilter in the body",  content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VacancyEntity.class))) }) 
	public Response getVacancies(
			@Parameter(name="language", description = "If langauge is not set, then the Vacancy default language is returned") @QueryParam("language") SupportedLanguage language,
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("100") @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		ArrayList<ApiFilterItem> filterItems = new ArrayList<ApiFilterItem>(1);
		filterItems.add(new ApiFilterItem(VacancyEntity_.ARCHIVED.toString(), "false", OperatorEnum.EQUALS));
		return apiServiceImpl.getVacancies(language, filterItems, offset, maxResults);
	}
	
	
	@GET 
	@Path("/getVacancy/{vacancyId}")	
	@ApiResponse (content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VacancyEntity.class))) }) 
	public Response getVacancy( @PathParam("vacancyId") int vacancyId, 
			@Parameter(name="language", description = "If langauge is not set, then the Vacancy default language is returned") @QueryParam("language") SupportedLanguage language,
			@Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		return apiServiceImpl.getVacancy(vacancyId, language);
	}
	
	
	
	@POST
	@Path("/getCandidates")
	@ApiResponse (description = "Get Candidates, optionally using the filter in the body",  content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CandidateEntity.class))) }) 
	public Response getCandidates(final List<ApiFilterItem> filterItems,
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("100") @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		return apiServiceImpl.getCandidates(filterItems, offset, maxResults);
	}
	
	@GET
	@Path("/getCandidates")
	@ApiResponse (description = "Get Candidates, optionally using the filter in the body",  content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CandidateEntity.class))) }) 
	public Response getCandidates(@DefaultValue("0") @QueryParam("offset") int offset,	@DefaultValue("100") @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		List<ApiFilterItem> filterItems = new ArrayList<ApiFilterItem>();
		return apiServiceImpl.getCandidates(filterItems, offset, maxResults);
	}
	
	@GET
	@Path("/getCandidateFiles")
	@ApiResponse (content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ApiRecruitingFile.class))) }) 
	public Response getCandidateFileList( @QueryParam("id") int candidateId, @QueryParam ("email") String email,
			@Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		return apiServiceImpl.getCandidateFileList(candidateId, email);
	}
	
	@POST
	@Path("/addCandidate")
	@ApiResponse (description = "Add Candidate",  content = { @Content(mediaType = "application/json", schema = @Schema(implementation = CandidateEntity.class)) }) 
	public Response addCandidate(CandidateEntity candidateEntity, @Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		return apiServiceImpl.addCandidate(candidateEntity);
	}
	
	@POST
	@Path("/addFileToCandidate")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@ApiResponse (description = "Get Candidates, optionally using the filter in the body",  content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ApiRecruitingFile.class)) }) 
	public Response addFileToCandidate(@QueryParam("id") int candidateId, @QueryParam ("email") String email,  @QueryParam ("fileName") String fileName, StreamingOutput streamingOutput,  @Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		return apiServiceImpl.addFileToCandidate (candidateId, email, fileName, streamingOutput);
	}	
	
	
	@POST
	@Path("/branchLocations")
	@ApiResponse (description = "Get company Branch Locations",  content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = BranchLocation.class))) }) 
	public Response getBranchLocations(final List<ApiFilterItem> filterItems, @DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("100") @QueryParam("maxResults") int maxResults, @Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		return apiServiceImpl.getEntityResults(filterItems, offset, maxResults, BranchLocation.class);
	}

	@POST
	@Path("/classifications")
	@ApiResponse (description = "Retrieves Vacancies, optionally using the ApiFilter in the body",  content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ClassificationEntity.class))) }) 
	public Response getClassifications(final List<ApiFilterItem> filterItems,
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("100") @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		return apiServiceImpl.getEntityResults(filterItems, offset, maxResults, ClassificationEntity.class);
	}
	
	@POST
	@Path("/careerLevels")
	@ApiResponse (description = "Retrieves CareerLevels, optionally using the ApiFilter in the body",  content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CareerLevelEntity.class))) }) 
	public Response getCareerLevels(final List<ApiFilterItem> filterItems,
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("100") @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		return apiServiceImpl.getEntityResults(filterItems, offset, maxResults, CareerLevelEntity.class);
	}
	
	@POST
	@Path("/getVacancyStatus")
	@ApiResponse (description = "Retrieves getVacancyStatus, optionally using the ApiFilter in the body",  content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VacancyStatusEntity.class))) }) 
	public Response getVacancyStatus(final List<ApiFilterItem> filterItems,
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("100") @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {
		ApiRecruitingImpl apiServiceImpl = CdiUtils.getReference(ApiRecruitingImpl.class);
		return apiServiceImpl.getEntityResults(filterItems, offset, maxResults, VacancyStatusEntity.class);
	}
		
	
	
	

}
