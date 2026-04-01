package com.zest.toeic.practice.seed;

import com.zest.toeic.practice.model.Question;
import com.zest.toeic.practice.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class QuestionSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(QuestionSeeder.class);
    private final QuestionRepository questionRepository;

    public QuestionSeeder(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) {
        if (questionRepository.countByStatus("PUBLISHED") > 0) {
            log.info("Questions already seeded, skipping. Count: {}", questionRepository.countByStatus("PUBLISHED"));
            return;
        }

        log.info("Seeding TOEIC questions...");
        List<Question> questions = new ArrayList<>();

        // Part 5: Incomplete Sentences (Grammar) — 60 questions
        seedPart5Grammar(questions);
        // Part 5: Vocabulary — 40 questions
        seedPart5Vocabulary(questions);
        // Part 6: Text Completion — 30 questions
        seedPart6(questions);
        // Part 7: Reading Comprehension — 30 questions
        seedPart7(questions);
        // Part 1: Photos — 10 questions
        seedPart1(questions);
        // Part 2: Question-Response — 15 questions
        seedPart2(questions);
        // Part 3/4: Conversations/Talks — 15 questions
        seedPart34(questions);

        questionRepository.saveAll(questions);
        log.info("Seeded {} TOEIC questions successfully!", questions.size());
    }

    private void seedPart5Grammar(List<Question> questions) {
        String[][] data = {
                {"The report _____ submitted by the deadline.", "A) was|B) were|C) is being|D) have been", "A", "EASY", "Past passive voice: singular subject 'report' → 'was'"},
                {"Neither the manager _____ the employees were informed.", "A) or|B) nor|C) and|D) but", "B", "EASY", "'Neither...nor' is the correct correlative conjunction"},
                {"She _____ working here since 2019.", "A) is|B) was|C) has been|D) had been", "C", "EASY", "Present perfect continuous with 'since'"},
                {"The meeting was postponed _____ the bad weather.", "A) because|B) due to|C) although|D) despite", "B", "EASY", "'Due to' + noun phrase for cause"},
                {"All employees must _____ the safety training.", "A) complete|B) completing|C) completed|D) to complete", "A", "EASY", "'Must' + base form of verb"},
                {"The project will be finished _____ next Friday.", "A) by|B) until|C) since|D) for", "A", "MEDIUM", "'By' indicates deadline completion"},
                {"_____ carefully reviewing the contract, she signed it.", "A) After|B) Before|C) While|D) During", "A", "MEDIUM", "'After' + gerund phrase for sequence"},
                {"The company has _____ announced its quarterly results.", "A) yet|B) already|C) still|D) ever", "B", "MEDIUM", "'Already' in present perfect affirmative"},
                {"He suggested that the team _____ the presentation.", "A) revise|B) revises|C) revised|D) revising", "A", "MEDIUM", "Subjunctive mood after 'suggest that'"},
                {"The new policy, _____ was introduced last month, has improved efficiency.", "A) that|B) which|C) who|D) whom", "B", "MEDIUM", "Non-restrictive clause uses 'which' with comma"},
                {"Had she known about the delay, she _____ earlier.", "A) would leave|B) would have left|C) will leave|D) had left", "B", "HARD", "Third conditional: past perfect + would have + past participle"},
                {"Not only _____ the project on time, but she also exceeded expectations.", "A) she completed|B) did she complete|C) she did complete|D) completed she", "B", "HARD", "Inversion after 'Not only' at start of sentence"},
                {"The CEO insists that every department _____ a detailed report.", "A) submit|B) submits|C) submitted|D) submitting", "A", "HARD", "Subjunctive mood: insist that + base form"},
                {"_____ the economic downturn, the company managed to grow.", "A) In spite|B) Despite of|C) Despite|D) Although of", "C", "MEDIUM", "'Despite' + noun/gerund (no 'of')"},
                {"The contract requires that all parties _____ in agreement.", "A) is|B) are|C) be|D) being", "C", "HARD", "Subjunctive 'be' after 'requires that'"},
                {"Sales have increased _____ 20% compared to last year.", "A) by|B) at|C) with|D) for", "A", "EASY", "'Increased by' for expressing rate of change"},
                {"The supervisor asked the team _____ overtime this weekend.", "A) work|B) working|C) to work|D) worked", "C", "EASY", "'Asked someone to do something'"},
                {"If I _____ the manager, I would approve the request.", "A) am|B) was|C) were|D) will be", "C", "MEDIUM", "Second conditional: 'If I were' (subjunctive)"},
                {"The goods _____ shipped before the invoice was issued.", "A) has been|B) had been|C) have been|D) was being", "B", "MEDIUM", "Past perfect passive: action before another past action"},
                {"Please find _____ the documents you requested.", "A) attach|B) attached|C) attaching|D) attachment", "B", "EASY", "'Find attached' — past participle as adjective"},
                {"The training session is _____ for all new hires.", "A) mandatory|B) mandating|C) mandated|D) mandate", "A", "EASY", "Adjective after 'is'"},
                {"_____ you have any questions, please contact HR.", "A) Should|B) Would|C) Could|D) Might", "A", "MEDIUM", "'Should' = 'If' in formal conditional"},
                {"The award was given to _____ contributed the most.", "A) whoever|B) whomever|C) who|D) whom", "A", "HARD", "'Whoever' as subject of clause"},
                {"We look forward to _____ from you soon.", "A) hear|B) hearing|C) heard|D) hears", "B", "EASY", "'Look forward to' + gerund"},
                {"The results were _____ better than expected.", "A) significant|B) significantly|C) significance|D) signify", "B", "EASY", "Adverb modifying adjective 'better'"},
                {"Mr. Kim, _____ is our senior analyst, will lead the project.", "A) that|B) which|C) who|D) whom", "C", "MEDIUM", "Non-restrictive clause for person → 'who'"},
                {"The budget must be approved _____ any spending occurs.", "A) after|B) before|C) during|D) while", "B", "EASY", "'Before' for prerequisite condition"},
                {"Each of the candidates _____ required to submit a resume.", "A) are|B) is|C) were|D) have been", "B", "MEDIUM", "'Each of' + singular verb"},
                {"The office will remain closed _____ further notice.", "A) until|B) by|C) since|D) for", "A", "EASY", "'Until further notice' — fixed expression"},
                {"_____ to the new system, all data must be backed up.", "A) Prior|B) Previous|C) Before of|D) Ahead", "A", "MEDIUM", "'Prior to' = 'before' (formal)"},
                {"The company is _____ considering expanding overseas.", "A) serious|B) seriously|C) seriousness|D) more serious", "B", "EASY", "Adverb modifying verb 'considering'"},
                {"They have yet _____ a decision on the merger.", "A) make|B) making|C) to make|D) made", "C", "MEDIUM", "'Have yet to' + infinitive"},
                {"The proposal was rejected _____ its high cost.", "A) because|B) due to|C) so that|D) in order to", "B", "EASY", "'Due to' + noun for reason"},
                {"_____ the instructions carefully before operating the machine.", "A) Read|B) Reading|C) To read|D) Reads", "A", "EASY", "Imperative: base form at start"},
                {"The director, along with two managers, _____ attending the conference.", "A) are|B) is|C) were|D) have been", "B", "HARD", "'Along with' doesn't change subject agreement — singular 'director' → 'is'"},
                {"This is the most _____ proposal we have received.", "A) promise|B) promising|C) promised|D) promisingly", "B", "EASY", "Adjective after 'most' for superlative"},
                {"The shipment is expected _____ by Thursday.", "A) arrive|B) arriving|C) to arrive|D) arrived", "C", "EASY", "'Expected to' + infinitive"},
                {"All staff are _____ to attend the meeting.", "A) require|B) required|C) requiring|D) requirement", "B", "EASY", "Passive: 'are required to'"},
                {"The report _____ by the time the meeting started.", "A) has been completed|B) had been completed|C) was completing|D) is completed", "B", "MEDIUM", "Past perfect passive: completed before meeting started"},
                {"_____ it rains tomorrow, the event will be held indoors.", "A) Unless|B) Should|C) If|D) Whether", "C", "EASY", "'If' + present for first conditional"},
                {"production costs, the company decided to outsource.", "A) To reduce|B) Reducing|C) Reduced|D) For reducing", "A", "MEDIUM", "'To reduce' — infinitive of purpose"},
                {"The new software is _____ user-friendly than the previous version.", "A) much|B) more|C) most|D) very", "B", "EASY", "Comparative: 'more' + multi-syllable adjective"},
                {"She is responsible _____ managing the marketing team.", "A) to|B) for|C) of|D) with", "B", "EASY", "'Responsible for' — fixed preposition"},
                {"The factory _____ down for maintenance next month.", "A) will shut|B) will be shut|C) is shutting|D) shuts", "B", "MEDIUM", "Future passive: 'will be shut'"},
                {"_____ the delay, all orders will be processed this week.", "A) Despite|B) Although|C) Because|D) Unless", "A", "EASY", "'Despite' + noun for contrast"},
                {"The regulations require employees _____ safety gear at all times.", "A) wear|B) wearing|C) to wear|D) worn", "C", "MEDIUM", "'Require someone to do'"},
                {"He is one of the most _____ employees in the company.", "A) dedicate|B) dedicated|C) dedicating|D) dedication", "B", "EASY", "Adjective (past participle) after 'most'"},
                {"The sales figures _____ significantly over the past quarter.", "A) improve|B) improves|C) improved|D) have improved", "D", "MEDIUM", "Present perfect with 'over the past quarter'"},
                {"Ms. Park is _____ charge of the finance department.", "A) on|B) in|C) at|D) by", "B", "EASY", "'In charge of' — fixed expression"},
                {"The deadline has been extended _____ two weeks.", "A) for|B) by|C) until|D) since", "B", "MEDIUM", "'Extended by' for duration of extension"},
                {"Applicants _____ a valid ID must present it at reception.", "A) have|B) having|C) had|D) who have", "B", "MEDIUM", "Reduced relative clause: 'having' = 'who have'"},
                {"The warranty covers repairs _____ defective parts.", "A) result from|B) resulting from|C) resulted from|D) results from", "B", "MEDIUM", "Present participle as adjective modifying 'repairs'"},
                {"_____ completion of the training, employees will receive a certificate.", "A) Upon|B) At|C) After of|D) In", "A", "MEDIUM", "'Upon completion' — formal preposition for 'when completed'"},
                {"The new hire is expected to _____ quickly to the work environment.", "A) adapt|B) adapting|C) adapted|D) adaptation", "A", "EASY", "'Expected to' + base form"},
                {"Customer satisfaction has _____ steadily over the past year.", "A) risen|B) raised|C) rose|D) raising", "A", "HARD", "'Risen' (intransitive) vs 'raised' (transitive)"},
                {"The committee will _____ the proposal at next week's meeting.", "A) discuss|B) discuss about|C) discussing|D) discussed", "A", "EASY", "'Discuss' is transitive — no preposition needed"},
                {"We need someone _____ experience in data analysis.", "A) who|B) with|C) has|D) having", "B", "EASY", "'Someone with experience' — prepositional phrase"},
                {"The marketing campaign was _____ successful than anticipated.", "A) less|B) least|C) lesser|D) few", "A", "EASY", "Comparative: 'less successful'"},
                {"Please ensure that all forms _____ correctly before submission.", "A) fill out|B) are filled out|C) filling out|D) filled out", "B", "MEDIUM", "Passive: 'forms are filled out'"},
                {"The venue can _____ up to 500 guests.", "A) accommodate|B) accommodating|C) accommodated|D) accommodation", "A", "EASY", "'Can' + base form"},
        };

        for (String[] q : data) {
            questions.add(buildQuestion(5, q[0], q[1], q[2], q[3], "GRAMMAR", q[4]));
        }
    }

    private void seedPart5Vocabulary(List<Question> questions) {
        String[][] data = {
                {"The company's annual _____ will be held in the convention center.", "A) revenue|B) conference|C) budget|D) inventory", "B", "EASY", "'Conference' = hội nghị, sự kiện hàng năm"},
                {"All employees are entitled to _____ health insurance.", "A) comprehensive|B) comprehend|C) comprehension|D) comprehensively", "A", "EASY", "'Comprehensive' = toàn diện (adjective)"},
                {"The manager will _____ the final decision after consulting the team.", "A) do|B) make|C) have|D) take", "B", "EASY", "'Make a decision' — collocation"},
                {"We need to _____ the proposal before submitting it.", "A) revise|B) revision|C) revised|D) revisable", "A", "EASY", "Verb form after 'to'"},
                {"Customer _____ is our top priority this quarter.", "A) satisfy|B) satisfying|C) satisfaction|D) satisfactory", "C", "EASY", "Noun after adjective (possessive)"},
                {"The product launch was a _____ success.", "A) remark|B) remarkable|C) remarkably|D) remarked", "B", "EASY", "Adjective before noun 'success'"},
                {"Please _____ the attached document for your reference.", "A) see|B) watch|C) look|D) refer", "A", "EASY", "'See the attached' — business English"},
                {"The store offers a wide _____ of products.", "A) vary|B) various|C) variety|D) varied", "C", "EASY", "'A wide variety of' — fixed expression"},
                {"The new regulation takes _____ on January 1st.", "A) place|B) effect|C) action|D) part", "B", "MEDIUM", "'Takes effect' = có hiệu lực"},
                {"The project is _____ schedule and within budget.", "A) in|B) on|C) at|D) by", "B", "EASY", "'On schedule' — fixed expression"},
                {"The consultant provided _____ advice on restructuring.", "A) value|B) valued|C) valuable|D) valuably", "C", "EASY", "Adjective before noun 'advice'"},
                {"Our quarterly _____ exceeded expectations.", "A) earn|B) earning|C) earnings|D) earned", "C", "EASY", "Noun (plural) as subject"},
                {"The merger is _____ to be completed by year-end.", "A) expect|B) expected|C) expecting|D) expectation", "B", "EASY", "Passive: 'is expected to'"},
                {"Please submit your _____ by the end of the week.", "A) apply|B) applicant|C) application|D) applicable", "C", "EASY", "Noun — 'submit your application'"},
                {"The department needs to _____ its spending.", "A) monitor|B) monitoring|C) monitored|D) monitors", "A", "EASY", "'Needs to' + base form"},
                {"The company _____ a significant increase in online sales.", "A) experienced|B) experience|C) experiencing|D) experiences", "A", "MEDIUM", "Past tense narrative"},
                {"All _____ must be pre-approved by the finance team.", "A) expend|B) expenses|C) expensive|D) expending", "B", "EASY", "Noun (plural) as subject"},
                {"The office renovation will _____ improve working conditions.", "A) signify|B) significant|C) significantly|D) significance", "C", "EASY", "Adverb modifying verb 'improve'"},
                {"The CEO's _____ inspired the entire workforce.", "A) lead|B) leading|C) leader|D) leadership", "D", "EASY", "Noun as subject — 'leadership'"},
                {"The contract includes a _____ clause for early termination.", "A) penalize|B) penalty|C) penalizing|D) penalized", "B", "MEDIUM", "Noun modifying noun: 'penalty clause'"},
                {"Employees are _____ to dress professionally.", "A) expect|B) expected|C) expecting|D) expectation", "B", "EASY", "Passive form"},
                {"The company _____ itself on excellent customer service.", "A) prides|B) proud|C) pride|D) proudly", "A", "MEDIUM", "'Prides itself on' — reflexive verb"},
                {"A thorough _____ of the system revealed several issues.", "A) inspect|B) inspector|C) inspection|D) inspecting", "C", "EASY", "Noun after adjective 'thorough'"},
                {"The proposal was _____ reviewed by the committee.", "A) thorough|B) thoroughly|C) thoroughness|D) more thorough", "B", "EASY", "Adverb modifying verb 'reviewed'"},
                {"We need to find a _____ solution to this problem.", "A) practice|B) practical|C) practically|D) practiced", "B", "EASY", "Adjective before noun 'solution'"},
                {"The company plans to _____ its operations to Asia.", "A) expand|B) expansion|C) expanding|D) expanded", "A", "EASY", "'Plans to' + base form"},
                {"All _____ information will be kept confidential.", "A) person|B) personal|C) personally|D) personalize", "B", "EASY", "Adjective before noun"},
                {"The team demonstrated great _____ during the project.", "A) collaborate|B) collaborative|C) collaboration|D) collaboratively", "C", "EASY", "Noun after 'great'"},
                {"The new policy is _____ to all departments.", "A) apply|B) applicable|C) application|D) applicant", "B", "EASY", "'Is applicable to' — adjective"},
                {"The renovation project was completed _____ of the original timeline.", "A) ahead|B) before|C) early|D) prior", "A", "MEDIUM", "'Ahead of' — fixed expression"},
                {"The supervisor _____ the team for their hard work.", "A) command|B) commended|C) commenced|D) committed", "B", "MEDIUM", "'Commended' = khen ngợi"},
                {"Client feedback has been _____ positive.", "A) overwhelm|B) overwhelming|C) overwhelmed|D) overwhelmingly", "D", "MEDIUM", "Adverb modifying adjective 'positive'"},
                {"The workshop aims to _____ employees' communication skills.", "A) enhance|B) enhancement|C) enhanced|D) enhancing", "A", "EASY", "'Aims to' + base form"},
                {"Attendance at the safety briefing is _____.", "A) mandate|B) mandatory|C) mandating|D) mandated", "B", "EASY", "Adjective after 'is'"},
                {"The investment yielded a _____ return.", "A) substance|B) substantial|C) substantially|D) substantiate", "B", "EASY", "Adjective before noun 'return'"},
                {"We sincerely _____ for any inconvenience caused.", "A) apologize|B) apology|C) apologetic|D) apologetically", "A", "EASY", "Verb after subject 'we'"},
                {"The _____ date for applications is March 15th.", "A) dead|B) deadline|C) deadly|D) deadlines", "B", "EASY", "Noun modifying noun: 'deadline date'"},
                {"Customers can track their orders _____.", "A) online|B) on line|C) on-lined|D) onlining", "A", "EASY", "Adverb 'online'"},
                {"The company _____ a press release about the merger.", "A) issue|B) issued|C) issuing|D) issues", "B", "EASY", "Past tense narrative"},
                {"All participants received a _____ of completion.", "A) certify|B) certificate|C) certification|D) certified", "B", "EASY", "'Certificate of completion' — noun"},
        };

        for (String[] q : data) {
            questions.add(buildQuestion(5, q[0], q[1], q[2], q[3], "VOCABULARY", q[4]));
        }
    }

    private void seedPart6(List<Question> questions) {
        String[][] data = {
                {"Dear Mr. Thompson, We are writing to _____ you that your order has been shipped.", "A) inform|B) information|C) informing|D) informed", "A", "EASY", "'To inform' — infinitive after 'writing to'"},
                {"The company _____ its commitment to sustainability in its annual report.", "A) highlight|B) highlighted|C) highlighting|D) highlights", "B", "EASY", "Past tense in context"},
                {"_____, the project team met all objectives ahead of schedule.", "A) Fortunate|B) Fortunately|C) Fortune|D) Fortuned", "B", "EASY", "Sentence adverb at start"},
                {"The renovation will take _____ three months to complete.", "A) approximate|B) approximately|C) approximation|D) approximated", "B", "EASY", "Adverb modifying number"},
                {"Employees who _____ overtime will receive additional compensation.", "A) work|B) works|C) working|D) worked", "A", "EASY", "'Who work' — present tense relative clause"},
                {"The marketing _____ presented a detailed campaign plan.", "A) depart|B) departure|C) department|D) departmental", "C", "EASY", "Noun as subject"},
                {"We are pleased to announce that our new branch will _____ next month.", "A) open|B) opening|C) opened|D) opens", "A", "EASY", "'Will' + base form"},
                {"The seminar provided _____ insights into market trends.", "A) value|B) valued|C) valuable|D) valuably", "C", "EASY", "Adjective before noun"},
                {"All safety _____ must be followed at all times.", "A) proceed|B) procedures|C) proceeding|D) process", "B", "EASY", "Noun — 'safety procedures'"},
                {"The CEO _____ the importance of teamwork during the meeting.", "A) emphasis|B) emphasize|C) emphasized|D) emphasizing", "C", "EASY", "Past tense verb"},
                {"Please review the _____ document before signing.", "A) attach|B) attached|C) attaching|D) attachment", "B", "EASY", "Past participle as adjective"},
                {"Our customer support team is _____ 24/7.", "A) avail|B) available|C) availability|D) availing", "B", "EASY", "'Is available' — adjective"},
                {"The _____ for the position closes on Friday.", "A) apply|B) applicant|C) application|D) applicable", "C", "EASY", "Noun as subject"},
                {"The manager _____ the new policy at yesterday's briefing.", "A) introduce|B) introduced|C) introducing|D) introduction", "B", "EASY", "Past tense"},
                {"We look forward to _____ you at the conference.", "A) see|B) seeing|C) seen|D) saw", "B", "EASY", "'Look forward to' + gerund"},
                {"The survey results _____ that customer satisfaction has increased.", "A) indicate|B) indication|C) indicative|D) indicating", "A", "MEDIUM", "Present tense verb as main predicate"},
                {"All employees should _____ their annual performance review.", "A) complete|B) completing|C) completed|D) completion", "A", "EASY", "'Should' + base form"},
                {"The company is committed to _____ high-quality products.", "A) provide|B) providing|C) provided|D) provision", "B", "EASY", "'Committed to' + gerund"},
                {"_____ receipt of your payment, we will process your order.", "A) In|B) Upon|C) At|D) With", "B", "MEDIUM", "'Upon receipt' — formal expression"},
                {"The financial report is due _____ the end of the quarter.", "A) by|B) until|C) since|D) for", "A", "EASY", "'Due by' — deadline"},
                {"The hotel offers _____ rates for corporate clients.", "A) discount|B) discounted|C) discounting|D) discounts", "B", "EASY", "Past participle as adjective before noun 'rates'"},
                {"We regret to inform you that your request has been _____.", "A) deny|B) denied|C) denying|D) denial", "B", "EASY", "Passive: 'has been denied'"},
                {"The workshop is designed to help participants _____ their leadership skills.", "A) develop|B) developing|C) developed|D) development", "A", "EASY", "'Help someone do/to do'"},
                {"The company _____ an annual charity event every December.", "A) host|B) hosts|C) hosting|D) hosted", "B", "MEDIUM", "Present tense habitual — 'every December'"},
                {"All _____ must be submitted through the online portal.", "A) inquire|B) inquiries|C) inquiring|D) inquiry", "B", "EASY", "Plural noun as subject"},
                {"The new software _____ users to automate repetitive tasks.", "A) enable|B) enables|C) enabling|D) enabled", "B", "MEDIUM", "Present tense third person singular"},
                {"Thank you for your _____ response to our inquiry.", "A) prompt|B) promptly|C) prompting|D) prompted", "A", "EASY", "Adjective before noun 'response'"},
                {"The agreement is _____ to change without prior notice.", "A) subject|B) subjected|C) subjecting|D) subjective", "A", "MEDIUM", "'Subject to' — fixed expression"},
                {"Employees are encouraged to _____ in professional development programs.", "A) participate|B) participating|C) participation|D) participant", "A", "EASY", "'Encouraged to' + base form"},
                {"The company achieved _____ growth in the Asian market.", "A) notice|B) noticeable|C) noticeably|D) noticed", "B", "EASY", "Adjective before noun 'growth'"},
        };

        for (String[] q : data) {
            questions.add(buildQuestion(6, q[0], q[1], q[2], q[3], "GRAMMAR", q[4]));
        }
    }

    private void seedPart7(List<Question> questions) {
        String[][] data = {
                {"According to the email, when is the deadline for submitting the report?", "A) March 1|B) March 15|C) April 1|D) April 15", "B", "EASY", "Detail question — scanning for date"},
                {"What is the main purpose of the memo?", "A) To announce a policy change|B) To request feedback|C) To introduce a new employee|D) To schedule a meeting", "A", "EASY", "Main idea question"},
                {"What is suggested about the company's new product?", "A) It is expensive|B) It is eco-friendly|C) It is only for professionals|D) It requires training", "B", "MEDIUM", "Inference question"},
                {"Who most likely wrote this notice?", "A) A customer|B) A supplier|C) A manager|D) An intern", "C", "EASY", "Author identification"},
                {"What will happen if the payment is not received by Friday?", "A) The order will be cancelled|B) A late fee will apply|C) The price will increase|D) The delivery will be delayed", "A", "MEDIUM", "Conditional detail question"},
                {"According to the article, what is the main advantage?", "A) Lower cost|B) Higher speed|C) Better quality|D) More features", "C", "EASY", "Specific detail retrieval"},
                {"What can be inferred about the sender?", "A) New to the company|B) Works in HR|C) Is a senior executive|D) Plans to resign", "C", "MEDIUM", "Inference from tone and content"},
                {"The word 'substantial' in paragraph 2 is closest in meaning to:", "A) small|B) considerable|C) temporary|D) optional", "B", "EASY", "Vocabulary in context"},
                {"What is NOT mentioned as a benefit of the program?", "A) Flexible hours|B) Free meals|C) Health insurance|D) Training opportunities", "B", "MEDIUM", "Negative detail question"},
                {"Where would this notice most likely be posted?", "A) In a newspaper|B) On a bulletin board|C) In a textbook|D) On social media", "B", "EASY", "Context inference"},
                {"What does the company plan to do next quarter?", "A) Expand to Europe|B) Reduce staff|C) Launch a new product|D) Close a branch", "C", "MEDIUM", "Future plan detail"},
                {"How long has the company been in business?", "A) 5 years|B) 10 years|C) 15 years|D) 20 years", "C", "EASY", "Specific detail question"},
                {"What is the tone of the letter?", "A) Apologetic|B) Demanding|C) Informative|D) Critical", "C", "EASY", "Tone identification"},
                {"What action is the reader asked to take?", "A) Call the office|B) Visit the website|C) Reply to the email|D) Submit documents", "C", "MEDIUM", "Action request identification"},
                {"Which department is responsible for handling complaints?", "A) Sales|B) Marketing|C) Customer Service|D) Finance", "C", "EASY", "Specific detail"},
                {"What does the speaker imply about the deadline?", "A) It is flexible|B) It cannot be extended|C) It has already passed|D) It depends on approval", "B", "HARD", "Implication question"},
                {"For whom is the announcement intended?", "A) Shareholders|B) All employees|C) New hires only|D) External partners", "B", "EASY", "Audience identification"},
                {"What change will take effect starting January?", "A) New pricing|B) Office relocation|C) Working hours|D) Benefits package", "C", "MEDIUM", "Detail about change"},
                {"What is indicated about the seminar?", "A) It is free|B) It requires registration|C) It is online only|D) It is for managers", "B", "MEDIUM", "Indicated detail"},
                {"What is the purpose of the attached form?", "A) To request leave|B) To report expenses|C) To provide feedback|D) To apply for transfer", "C", "EASY", "Purpose identification"},
                {"Based on the chart, which quarter had the highest sales?", "A) Q1|B) Q2|C) Q3|D) Q4", "C", "EASY", "Chart reading question"},
                {"What condition must be met for the warranty to apply?", "A) Purchase within 30 days|B) Original receipt required|C) Online registration|D) Product unused", "B", "MEDIUM", "Conditional detail"},
                {"Why was the event postponed?", "A) Low attendance|B) Weather conditions|C) Venue issues|D) Budget constraints", "C", "EASY", "Reason question"},
                {"What does the company guarantee?", "A) Fastest delivery|B) Money-back if unsatisfied|C) Free returns|D) 24/7 support", "B", "EASY", "Promise/guarantee detail"},
                {"Which statement is true about the new policy?", "A) It applies to full-time only|B) It takes effect immediately|C) It was approved unanimously|D) It replaces all prior policies", "B", "MEDIUM", "True statement identification"},
                {"The notice suggests that interested applicants should:", "A) Submit online|B) Call directly|C) Visit in person|D) Send a letter", "A", "EASY", "Recommendation detail"},
                {"What problem is mentioned in the report?", "A) Staffing shortage|B) Equipment failure|C) Supply delay|D) Budget overrun", "C", "MEDIUM", "Problem identification"},
                {"According to the schedule, when does the workshop begin?", "A) 8:00 AM|B) 9:30 AM|C) 10:00 AM|D) 1:00 PM", "B", "EASY", "Schedule reading"},
                {"What advantage does the premium plan offer?", "A) 24/7 support|B) Free updates|C) Priority access|D) All of the above", "D", "EASY", "Detail comparison"},
                {"What is the main topic of the article?", "A) Technology trends|B) Company earnings|C) Employee wellness|D) Market competition", "C", "EASY", "Main topic question"},
        };

        for (String[] q : data) {
            questions.add(buildQuestion(7, q[0], q[1], q[2], q[3], "READING", q[4]));
        }
    }

    private void seedPart1(List<Question> questions) {
        String[][] data = {
                {"[Photo: People in a meeting room] What is happening in the photo?", "A) People are leaving the office|B) People are having a meeting|C) The room is empty|D) Someone is on the phone", "B", "EASY", "Part 1: Describe what you see"},
                {"[Photo: Woman at desk] What is the woman doing?", "A) She is typing on a computer|B) She is drinking coffee|C) She is reading a book|D) She is talking on the phone", "A", "EASY", "Part 1: Action description"},
                {"[Photo: Construction site] Where was this photo taken?", "A) In a park|B) At a construction site|C) In a hospital|D) At a school", "B", "EASY", "Part 1: Location identification"},
                {"[Photo: People waiting at airport] What are the people doing?", "A) Boarding a plane|B) Checking luggage|C) Waiting in line|D) Running to the gate", "C", "EASY", "Part 1: Action description"},
                {"[Photo: Stacked boxes in warehouse] What can be seen in the photo?", "A) Books on shelves|B) Boxes stacked in a warehouse|C) Cars in a parking lot|D) Plants in a garden", "B", "EASY", "Part 1: Object identification"},
                {"[Photo: Chef cooking] What is the person doing?", "A) Serving food|B) Washing dishes|C) Preparing food|D) Setting a table", "C", "MEDIUM", "Part 1: Specific action"},
                {"[Photo: Empty conference room] What is shown in the photo?", "A) A busy office|B) An empty meeting room|C) A classroom with students|D) A restaurant", "B", "EASY", "Part 1: Scene description"},
                {"[Photo: Man presenting] What is the man doing?", "A) Writing on a whiteboard|B) Giving a presentation|C) Reading a newspaper|D) Fixing a computer", "B", "EASY", "Part 1: Action description"},
                {"[Photo: Cars on highway] What does the photo show?", "A) A parking lot|B) Traffic on a highway|C) A train station|D) An empty road", "B", "EASY", "Part 1: Scene identification"},
                {"[Photo: People shaking hands] What is happening?", "A) People are arguing|B) People are eating|C) People are greeting each other|D) People are exercising", "C", "EASY", "Part 1: Interaction description"},
        };

        for (String[] q : data) {
            questions.add(buildQuestion(1, q[0], q[1], q[2], q[3], "LISTENING", q[4]));
        }
    }

    private void seedPart2(List<Question> questions) {
        String[][] data = {
                {"Where is the nearest post office?", "A) It's on Main Street|B) At 3 o'clock|C) Yes, I do", "A", "EASY", "Part 2: Location question"},
                {"When does the meeting start?", "A) In the conference room|B) At 2:00 PM|C) Mr. Johnson", "B", "EASY", "Part 2: Time question"},
                {"Who is responsible for the report?", "A) Last Friday|B) The marketing team|C) On the third floor", "B", "EASY", "Part 2: Who question"},
                {"How do you get to the airport?", "A) At terminal 3|B) By taxi or subway|C) Around 8 AM", "B", "EASY", "Part 2: How question"},
                {"Would you like coffee or tea?", "A) Yes, I would|B) Tea, please|C) No, thank you for the offer", "B", "EASY", "Part 2: Choice question"},
                {"Have you finished the proposal?", "A) At the office|B) Almost, I need one more hour|C) For the client", "B", "MEDIUM", "Part 2: Yes/No question"},
                {"Why was the shipment delayed?", "A) Next Tuesday|B) Due to a customs issue|C) To the warehouse", "B", "MEDIUM", "Part 2: Reason question"},
                {"Could you send me the updated schedule?", "A) Sure, I'll email it now|B) Three meetings|C) In the morning", "A", "EASY", "Part 2: Request response"},
                {"How many copies do we need?", "A) The printer is broken|B) About fifty|C) In color", "B", "EASY", "Part 2: Quantity question"},
                {"Isn't the deadline tomorrow?", "A) No, it was extended to Friday|B) The deadline is important|C) I submitted mine", "A", "MEDIUM", "Part 2: Negative question"},
                {"What time does the store close?", "A) On weekdays|B) At 9 PM|C) Near the mall", "B", "EASY", "Part 2: Time question"},
                {"Where should I park?", "A) About 30 minutes|B) The lot behind the building|C) With the manager", "B", "EASY", "Part 2: Location question"},
                {"Who should I contact about the refund?", "A) Last week|B) Customer service on the first floor|C) The blue one", "B", "EASY", "Part 2: Contact question"},
                {"Don't we need approval first?", "A) Yes, I'll get it from the director|B) The form is on the desk|C) It costs $500", "A", "MEDIUM", "Part 2: Confirmation question"},
                {"Which restaurant do you recommend?", "A) For lunch|B) The Italian place on 5th Avenue|C) Around noon", "B", "EASY", "Part 2: Recommendation question"},
        };

        for (String[] q : data) {
            // Part 2 has only 3 options
            questions.add(buildQuestion3Options(2, q[0], q[1], q[2], q[3], "LISTENING", q[4]));
        }
    }

    private void seedPart34(List<Question> questions) {
        String[][] data = {
                {"What are the speakers mainly discussing?", "A) A new project|B) Vacation plans|C) Office supplies|D) Lunch options", "A", "EASY", "Part 3: Main topic"},
                {"What does the woman suggest?", "A) Postponing the event|B) Hiring more staff|C) Changing the venue|D) Cancelling the order", "C", "MEDIUM", "Part 3: Suggestion"},
                {"Where does the conversation most likely take place?", "A) At a restaurant|B) In an office|C) At a hospital|D) In a store", "B", "EASY", "Part 3: Location inference"},
                {"What will the man probably do next?", "A) Make a phone call|B) Write a report|C) Leave the office|D) Attend a meeting", "A", "MEDIUM", "Part 3: Next action prediction"},
                {"Why is the woman concerned?", "A) The budget is limited|B) The deadline is approaching|C) The quality is poor|D) The team is too small", "B", "MEDIUM", "Part 3: Reason for concern"},
                {"What is the talk mainly about?", "A) Company history|B) Safety procedures|C) Financial results|D) New products", "B", "EASY", "Part 4: Main topic"},
                {"Who is the intended audience?", "A) Customers|B) Shareholders|C) New employees|D) Suppliers", "C", "EASY", "Part 4: Audience identification"},
                {"What will happen next week?", "A) A training session|B) An audit|C) A product launch|D) A holiday", "A", "EASY", "Part 4: Future event"},
                {"What is mentioned about the new system?", "A) It is expensive|B) It is user-friendly|C) It requires training|D) It is temporary", "C", "MEDIUM", "Part 4: Detail recall"},
                {"What should listeners do if they have questions?", "A) Email the HR department|B) Contact their supervisor|C) Visit the website|D) Call the helpdesk", "B", "MEDIUM", "Part 4: Action instruction"},
                {"What problem does the speaker mention?", "A) Late deliveries|B) Staff turnover|C) Equipment malfunction|D) Customer complaints", "D", "MEDIUM", "Part 4: Problem identification"},
                {"According to the speaker, what changed last quarter?", "A) Working hours|B) Management structure|C) Company policy|D) Office location", "C", "MEDIUM", "Part 4: Change detail"},
                {"What does the man offer to do?", "A) Help with the report|B) Drive to the airport|C) Cover for a colleague|D) Order supplies", "A", "EASY", "Part 3: Offer"},
                {"What does the woman imply?", "A) She agrees|B) She is uncertain|C) She disagrees|D) She doesn't care", "B", "HARD", "Part 3: Implication"},
                {"How will the information be distributed?", "A) By email|B) In a meeting|C) On the notice board|D) Through the website", "A", "EASY", "Part 4: Distribution method"},
        };

        for (int i = 0; i < data.length; i++) {
            String[] q = data[i];
            int part = i < 5 ? 3 : (i < 10 ? 4 : (i < 14 ? 3 : 4));
            questions.add(buildQuestion(part, q[0], q[1], q[2], q[3], "LISTENING", q[4]));
        }
    }

    private Question buildQuestion(int part, String content, String optionsStr, String correct, String difficulty, String category, String explanation) {
        String[] opts = optionsStr.split("\\|");
        var options = new ArrayList<Question.QuestionOption>();
        for (String opt : opts) {
            String label = opt.trim().substring(0, 1);
            String text = opt.trim().substring(3);
            options.add(Question.QuestionOption.builder().label(label).text(text).build());
        }

        return Question.builder()
                .part(part)
                .content(content)
                .options(options)
                .correctAnswer(correct)
                .difficulty(difficulty)
                .category(category)
                .explanation(explanation)
                .status("PUBLISHED")
                .source("manual")
                .build();
    }

    private Question buildQuestion3Options(int part, String content, String optionsStr, String correct, String difficulty, String category, String explanation) {
        String[] opts = optionsStr.split("\\|");
        var options = new ArrayList<Question.QuestionOption>();
        for (String opt : opts) {
            String label = opt.trim().substring(0, 1);
            String text = opt.trim().substring(3);
            options.add(Question.QuestionOption.builder().label(label).text(text).build());
        }

        return Question.builder()
                .part(part)
                .content(content)
                .options(options)
                .correctAnswer(correct)
                .difficulty(difficulty)
                .category(category)
                .explanation(explanation)
                .status("PUBLISHED")
                .source("manual")
                .build();
    }
}
