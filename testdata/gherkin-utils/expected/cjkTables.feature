Feature: hello

  Scenario: one
    Given a data table with CJK:
      | 路     | numbers |
      | 路     |       1 |
      | 路步   |      10 |
      | 路步路 |     100 |
    Given a data table with Korean:
      | 훈       | numbers |
      | 기를육   |       1 |
      | 가르칠훈 |      10 |
    Given a data table with fullwidth:
      | Ａ     | numbers |
      | Ａ     |       1 |
      | ＡＢ   |      10 |
      | ＡＢＣ |     100 |
