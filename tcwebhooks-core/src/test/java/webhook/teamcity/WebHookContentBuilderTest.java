package webhook.teamcity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildHistory;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SFinishedBuild;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import webhook.WebHook;
import webhook.WebHookImpl;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.WebHookTemplateManager;
import webhook.teamcity.payload.WebHookTemplateResolver;
import webhook.teamcity.payload.content.WebHookPayloadContentAssemblyException;
import webhook.teamcity.payload.format.WebHookPayloadJson;
import webhook.teamcity.settings.WebHookConfig;

public class WebHookContentBuilderTest {
	
	SFinishedBuild previousSuccessfulBuild = mock(SFinishedBuild.class);
	SFinishedBuild previousFailedBuild = mock(SFinishedBuild.class);
	List<SFinishedBuild> finishedSuccessfulBuilds = new ArrayList<>();
	List<SFinishedBuild> finishedFailedBuilds = new ArrayList<>();
	MockSBuildType sBuildType = new MockSBuildType("Test Build", "A Test Build", "bt1");
	MockSRunningBuild sRunningBuild = new MockSRunningBuild(sBuildType, "SubVersion", Status.NORMAL, "Running", "TestBuild01");
	MockSProject sProject = new MockSProject("Test Project", "A test project", "project1", "ATestProject", sBuildType);
	BuildHistory buildHistory = mock(BuildHistory.class);
	SBuildServer server = mock(SBuildServer.class);

	@Before
	public void setUp() throws Exception {
		when(previousSuccessfulBuild.getBuildStatus()).thenReturn(Status.NORMAL);
		when(previousSuccessfulBuild.isPersonal()).thenReturn(false);
		when(previousFailedBuild.getBuildStatus()).thenReturn(Status.FAILURE);
		when(previousFailedBuild.isPersonal()).thenReturn(false);
		when(buildHistory.getEntriesBefore(sRunningBuild, false)).thenReturn(finishedSuccessfulBuilds);
		when(server.getHistory()).thenReturn(buildHistory);
		sBuildType.setProject(sProject);
	}

	@Test @Ignore
	public void testBuildWebHookContent() throws WebHookPayloadContentAssemblyException {
		
		WebHookPayloadManager manager = new WebHookPayloadManager(server);
		WebHookPayloadJson whp = new WebHookPayloadJson(manager);
		whp.register();
		WebHookTemplateManager webHookTemplateManager = new WebHookTemplateManager(manager);
		WebHookTemplateResolver resolver = new WebHookTemplateResolver(webHookTemplateManager);
		WebHookContentBuilder builder = new WebHookContentBuilder(server, manager, resolver);
		WebHook wh = new WebHookImpl();
		WebHookConfig whc = mock(WebHookConfig.class);
		when(whc.getPayloadFormat()).thenReturn("JSON");
		
		wh = builder.buildWebHookContent(wh, whc, sRunningBuild, BuildStateEnum.BUILD_FINISHED, true);
		System.out.println(wh.getContent());
		fail("Not yet implemented");
	}
	
	@Test
	public void testGetPreviousNonPreviousNonPersonalBuild_WhenPreviousIsPersonal() {
		WebHookContentBuilder builder = new WebHookContentBuilder(null, null, null);
		WebHook wh = new WebHookImpl();
		
		SBuild runningBuild = mock(SBuild.class);
		SFinishedBuild personalPreviousBuild = mock(SFinishedBuild.class);
		SFinishedBuild nonPersonalPreviousBuild = mock(SFinishedBuild.class);
		
		when(runningBuild.getPreviousFinished()).thenReturn(personalPreviousBuild);
		when(runningBuild.getBuildId()).thenReturn(100L);
		when(personalPreviousBuild.isPersonal()).thenReturn(true);
		when(personalPreviousBuild.getBuildId()).thenReturn(99L);
		when(personalPreviousBuild.getPreviousFinished()).thenReturn(nonPersonalPreviousBuild);
		when(nonPersonalPreviousBuild.getBuildId()).thenReturn(98L);
		
		SBuild previousBuild = builder.getPreviousNonPersonalBuild(wh, runningBuild);
		assertEquals(nonPersonalPreviousBuild, previousBuild);
		assertEquals(nonPersonalPreviousBuild, wh.getPreviousNonPersonalBuild());
		assertEquals(98L, previousBuild.getBuildId());
		
	}
	
	@Test
	public void testGetPreviousNonPreviousNonPersonalBuild_WhenPreviousIsNonPersonal() {
		WebHookContentBuilder builder = new WebHookContentBuilder(null, null, null);
		WebHook wh = new WebHookImpl();
		
		SBuild runningBuild = mock(SBuild.class);
		SFinishedBuild nonPersonalPreviousBuild = mock(SFinishedBuild.class);
		
		when(runningBuild.getPreviousFinished()).thenReturn(nonPersonalPreviousBuild);
		when(runningBuild.getBuildId()).thenReturn(100L);
		when(nonPersonalPreviousBuild.getBuildId()).thenReturn(98L);
		
		SBuild previousBuild = builder.getPreviousNonPersonalBuild(wh, runningBuild);
		assertEquals(nonPersonalPreviousBuild, previousBuild);
		assertEquals(nonPersonalPreviousBuild, wh.getPreviousNonPersonalBuild());
		assertEquals(98L, previousBuild.getBuildId());
		
	}
	
	@Test
	public void testGetPreviousNonPreviousNonPersonalBuild_WhenPersonalPreviousReturnsNull() {
		WebHookContentBuilder builder = new WebHookContentBuilder(null, null, null);
		WebHook wh = new WebHookImpl();
		
		SBuild runningBuild = mock(SBuild.class);
		SFinishedBuild personalPreviousBuild = mock(SFinishedBuild.class);
		
		when(runningBuild.getPreviousFinished()).thenReturn(personalPreviousBuild);
		when(runningBuild.getBuildId()).thenReturn(100L);
		when(personalPreviousBuild.isPersonal()).thenReturn(true);
		when(personalPreviousBuild.getBuildId()).thenReturn(99L);
		when(personalPreviousBuild.getPreviousFinished()).thenReturn(null);
		
		SBuild previousBuild = builder.getPreviousNonPersonalBuild(wh, runningBuild);
		assertNull(previousBuild);
		assertNull(wh.getPreviousNonPersonalBuild());
		
	}
	
	@Test
	public void testGetPreviousNonPreviousNonPersonalBuild_WhenNonPersonalPreviousReturnsNull() {
		WebHookContentBuilder builder = new WebHookContentBuilder(null, null, null);
		WebHook wh = new WebHookImpl();
		
		SBuild runningBuild = mock(SBuild.class);
		
		when(runningBuild.getPreviousFinished()).thenReturn(null);
		when(runningBuild.getBuildId()).thenReturn(100L);
		
		SBuild previousBuild = builder.getPreviousNonPersonalBuild(wh, runningBuild);
		assertNull(previousBuild);
		assertNull(wh.getPreviousNonPersonalBuild());
		
	}

}
