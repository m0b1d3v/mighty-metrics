package dev.m0b1.mighty.metrics.route;

import dev.m0b1.mighty.metrics.auth.AuthUtil;
import dev.m0b1.mighty.metrics.db.score.DbScoreRepository;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCard;
import dev.m0b1.mighty.metrics.db.scorecard.DbScoreCardRepository;
import dev.m0b1.mighty.metrics.statistics.ServiceStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class RouteIndex {

  private static final String PATH = "/";

  private final DbScoreRepository dbScoreRepository;
  private final DbScoreCardRepository dbScoreCardRepository;
  private final ServiceStatistics serviceStatistics;

  @GetMapping(PATH)
  public String index(
    @AuthenticationPrincipal OAuth2User user,
    Model model
  ) {

    var view = "index";

    if (user != null) {
      populateMemberData(user, model);
      view = "member";
    }

    return view;
  }

  private void populateMemberData(OAuth2User user, Model model) {
    var idMember = AuthUtil.getUserIdIfAttributePresent(user);
    var scorecards = populateScorecards(idMember, model);
    populateStatistics(scorecards, model);
    populateScores(model);
  }

  private List<DbScoreCard> populateScorecards(Long idMember, Model model) {
    var scorecards = dbScoreCardRepository.readAll(idMember);
    model.addAttribute("scorecards", scorecards);
    return scorecards;
  }

  private void populateStatistics(List<DbScoreCard> scorecards, Model model) {
    var statistics = serviceStatistics.generate(scorecards);
    model.addAttribute("statistics", statistics);
  }

  private void populateScores(Model model) {
    var scores = dbScoreRepository.read();
    model.addAttribute("scores", scores);
  }

}
