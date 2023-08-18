package org.springframework.ai.openai.client;

import org.junit.jupiter.api.Test;
import org.springframework.ai.client.Generation;
import org.springframework.ai.prompt.Prompt;
import org.springframework.ai.prompt.PromptTemplate;
import org.springframework.ai.prompt.SystemPromptTemplate;
import org.springframework.ai.prompt.messages.Message;
import org.springframework.ai.prompt.messages.SystemMessage;
import org.springframework.ai.prompt.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClientIntegrationTests {

	@Autowired
	OpenAiClient openAiClient;

	@Value("classpath:/prompts/system-message.st")
	private Resource systemResource;

	@Value("classpath:/prompts/system-evaluator-message.st")
	private Resource systemEvaluatorResource;

	@Value("classpath:/prompts/user-evaluator-message.st")
	private Resource userEvaluatorResource;

	@Test
	void roleTest() {
		String request = "Tell me about 3 famous pirates from the Golden Age of Piracy and why they did.";
		String name = "Bob";
		String voice = "pirate";
		UserMessage userMessage = new UserMessage(request);
		SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
		Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", name, "voice", voice));
		Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
		Generation response = openAiClient.generate(prompt).getGeneration();
		System.out.println(response);
		assertThat(response).isNotNull();

		evaluateQuestionAndAnswer(request, response.getText());

	}

	private void evaluateQuestionAndAnswer(String question, String answer) {
		PromptTemplate userPromptTemplate = new PromptTemplate(userEvaluatorResource,
				Map.of("question", question, "answer", answer));
		SystemMessage systemMessage = new SystemMessage(systemEvaluatorResource);
		Message userMessage = userPromptTemplate.createMessage();
		Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
		Generation response = openAiClient.generate(prompt).getGeneration();
		System.out.println(response);
		assertThat(response.getText()).isEqualTo("YES");
	}

}
