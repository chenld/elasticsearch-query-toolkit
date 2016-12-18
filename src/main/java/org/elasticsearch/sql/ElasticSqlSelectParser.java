package org.elasticsearch.sql;

import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.SQLSelectParser;
import com.alibaba.druid.sql.parser.Token;

public class ElasticSqlSelectParser extends SQLSelectParser {

    public ElasticSqlSelectParser(SQLExprParser exprParser) {
        super(exprParser);
    }

    public ElasticSqlSelectQueryBlock.Limit parseLimit() {
        return ((ElasticSqlExprParser) this.exprParser).parseLimit();
    }

    @Override
    public SQLSelectQuery query() {
        if (lexer.token() == Token.LPAREN) {
            lexer.nextToken();
            SQLSelectQuery select = query();
            accept(Token.RPAREN);
            return queryRest(select);
        }

        accept(Token.SELECT);

        if (lexer.token() == Token.COMMENT) {
            lexer.nextToken();
        }

        ElasticSqlSelectQueryBlock queryBlock = new ElasticSqlSelectQueryBlock();

        if (lexer.token() == Token.DISTINCT) {
            queryBlock.setDistionOption(SQLSetQuantifier.DISTINCT);
            lexer.nextToken();
        } else if (lexer.token() == Token.UNIQUE) {
            queryBlock.setDistionOption(SQLSetQuantifier.UNIQUE);
            lexer.nextToken();
        } else if (lexer.token() == Token.ALL) {
            queryBlock.setDistionOption(SQLSetQuantifier.ALL);
            lexer.nextToken();
        }

        parseSelectList(queryBlock);
        parseFrom(queryBlock);
        parseWhere(queryBlock);
        parseGroupBy(queryBlock);
        queryBlock.setOrderBy(this.exprParser.parseOrderBy());

        if (lexer.token() == Token.LIMIT) {
            queryBlock.setLimit(parseLimit());
        }

        return queryRest(queryBlock);
    }
}